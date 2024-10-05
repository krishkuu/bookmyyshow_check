package com.example.Bookmyyshow.service;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.Bookmyyshow.bean.BookingResponse;
import com.example.Bookmyyshow.bean.Bookingrequest;
import com.example.Bookmyyshow.bean.Movie;
import com.example.Bookmyyshow.bean.User;
import com.example.Bookmyyshow.exceptionhandler.UserNotFoundException;
import com.example.Bookmyyshow.repost.MovieRepository;
import com.example.Bookmyyshow.repost.ShowtimeRepository;
import com.example.Bookmyyshow.repost.UserRepo;
import com.example.Bookmyyshow.repost.bookingResponseRepository;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;  // Correct Document import
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class Movieservice {

    @Autowired
    private bookingResponseRepository bookingResponseRepository;

    @Autowired
    private MovieRepository movieRepository;
    
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ShowtimeRepository  showtimeRepository;
   // Assuming PVR has a different base URL
    @Autowired
	JavaMailSender mailSender;

	String pvrBaseUrl = "http://localhost:8080/pvr";
	String agsBaseUrl = "http://localhost:8081/ags";

    private String getBaseUrl(String cinemaName) {
        switch (cinemaName.toLowerCase()) {
            case "ags":
                return agsBaseUrl;
            case "pvr":
                return pvrBaseUrl;
            default:
                throw new IllegalArgumentException("Unsupported cinema name: " + cinemaName);
        }
    }

    /* Get List of Movies */
    public List<Movie> getMovies(String cinemaName) {
        String url = getBaseUrl(cinemaName) + "/movies";
        return performGetRequest(url, new ParameterizedTypeReference<List<Movie>>() {});
    }

    /* Get Movie by Id */
    public Movie getMovieById(String cinemaName, String movieId) {
        String url = getBaseUrl(cinemaName) + "/movies/" + movieId;
        return performGetRequest(url, Movie.class);
    }

    /* Get List of Users */
    public List<User> getUserList() {
        return userRepo.findAll();
    }

    /* Get User by Id */
    public User getUserById(String userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with userId " + userId + " not found"));
    }

    /* Check Movie Seats */
    public Movie checkSeats(String cinemaName, String movieId, String showTime) {
        String url = getBaseUrl(cinemaName) + "/checkSeats/" + movieId + "/" + showTime;
        return performGetRequest(url, Movie.class);
    }
    /* Book Seats */
    public BookingResponse bookSeats(String cinemaName, Bookingrequest bookingRequest) {
        // Validate the user
        User user = validateUser(bookingRequest.getUserId());

        // Build the URL for booking
        String url = getBaseUrl(cinemaName) + "/bookTickets";

        // Perform POST request to book the seats
        BookingResponse bookingResponse = performPostRequest(url, bookingRequest, BookingResponse.class);

        // Check if bookingResponse is not null and save it
        if (bookingResponse != null) {
            // Save the booking response to the database
            bookingResponseRepository.save(bookingResponse);
            
            // Generate PDF and send an email
            try {
                // Generate PDF
                byte[] pdfBytes = generatePdf(bookingResponse, cinemaName);

                // Send email with the PDF attachment to the user's email
                String userEmail = user.getEmail(); // Assuming `User` class has an `email` field
                sendEmailWithAttachment(userEmail, "Your Movie Ticket", "Please find your movie ticket attached.", pdfBytes);

                // Log successful email send
                System.out.println("Email sent successfully to: " + userEmail);
            } catch (Exception e) {
                // Handle any exceptions that occur during PDF generation or email sending
                System.err.println("Error generating PDF or sending email: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("Booking response is null");
        }
        return bookingResponse;
    }

//for cancel booking
    public String cancelBooking(String cinemaName,String bookingId)
    {
    	if (cinemaName.equals("pvr") || cinemaName.equals("PVR")) {
			String url = pvrBaseUrl + "/cancelBooking/" + bookingId;
			if (restTemplate.getForObject(url, String.class) != null) {
				Optional<BookingResponse> bookingOptional = bookingResponseRepository.findById(bookingId);
				if (!bookingOptional.isPresent())
					throw new UserNotFoundException("Booking Id not found");
				BookingResponse response = bookingOptional.get();
				response.setStatus("Cancelled");
				bookingResponseRepository.save(response);
			}
			return "Cancelled";
		} else if (cinemaName.equals("ags") || cinemaName.equals("AGS")) {
			String url = agsBaseUrl + "/cancelBooking/" + bookingId;
			if (restTemplate.getForObject(url, String.class) != null) {
				Optional<BookingResponse> bookingOptional = bookingResponseRepository.findById(bookingId);
				if (!bookingOptional.isPresent())
					throw new UserNotFoundException("Booking Id not found");
				BookingResponse response = bookingOptional.get();
				response.setStatus("Cancelled"); 
				bookingResponseRepository.save(response);
			}
			return "Cancelled";
		}
		return null;
    }

//to genrate pdf when u give json request for book ticket it automatically generated as pdf format
	public void generatePdf(HttpServletResponse response, BookingResponse bookingResponse, String cinemaName) {
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=report.pdf");

		try {
			PdfWriter writer = new PdfWriter(response.getOutputStream());
			PdfDocument pdf = new PdfDocument(writer);
			Document document = new Document(pdf, PageSize.A4);

			float fontSize = 12f;

			Text confirmation = new Text("Ticket Confirmed\n").setFontSize(30f).setBold().setUnderline()
					.setFontColor(new DeviceRgb(255, 0, 0));

			Text cineName = new Text("Booked At: --\n\n").setFontSize(22f).setTextAlignment(TextAlignment.CENTER);
			if (cinemaName.equals("pvr") || cinemaName.equals("PVR")) {
				cineName = new Text("Booked At: PVR CINEMAS\n\n").setFontSize(22f)
						.setTextAlignment(TextAlignment.CENTER);
			} else if (cinemaName.equals("ags") || cinemaName.equals("AGS")) {
				cineName = new Text("Booked At: AGS CINEMAS\n\n").setFontSize(22f)
						.setTextAlignment(TextAlignment.CENTER);
			}

			Text bookedBy = new Text("Booked Through: TICKETNEW\n").setFontSize(25f)
					.setTextAlignment(TextAlignment.CENTER);
			Text bookingIdText = new Text("Booking Id: \t" + bookingResponse.getBookingId() + "\n")
					.setFontSize(fontSize);

			Text movieNameText = new Text("Movie Name: \t" + bookingResponse.getMovieName() + "\n")
					.setFontSize(fontSize);

			Text movieIdText = new Text("Movie Id: \t" + bookingResponse.getMovieId() + "\n").setFontSize(fontSize);

			Text showTimeText = new Text("Movie Time: \t" + bookingResponse.getShowTime() + "\n").setFontSize(fontSize);

			Text seatsBookedText = new Text("Seats Booked: \t" + bookingResponse.getNumberOfTickets() + "\n")
					.setFontSize(fontSize);

			Text currencyText = new Text("Currency: \t" + bookingResponse.getCurrency() + "\n").setFontSize(fontSize);

			Text priceText = new Text("Price: \t" + bookingResponse.getTotalAmount() + "\n").setFontSize(fontSize);

			Text statusText = new Text("Status: \t" + bookingResponse.getStatus() + "\n").setFontSize(fontSize);

			// Add all text elements to the paragraph
			Paragraph paragraph = new Paragraph().add(confirmation).add(bookedBy).add(cineName).add(bookingIdText)
					.add(movieNameText).add(movieIdText).add(showTimeText).add(seatsBookedText).add(currencyText)
					.add(priceText).add(statusText);

			// Optionally, align text to the center
			paragraph.setTextAlignment(TextAlignment.LEFT);

			document.add(paragraph.setFontColor(new DeviceRgb(0, 0, 0)));
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public byte[] generatePdf(BookingResponse bookingResponse, String cinemaName) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			PdfWriter writer = new PdfWriter(outputStream);
			PdfDocument pdf = new PdfDocument(writer);
			Document document = new Document(pdf, PageSize.A4);

			float fontSize = 12f;

			Text confirmation = new Text("Ticket Confirmed\n").setFontSize(30f).setBold().setUnderline()
					.setFontColor(new DeviceRgb(255, 0, 0));

			Text cineName = new Text("Booked At: --\n\n").setFontSize(22f).setTextAlignment(TextAlignment.CENTER);
			if (cinemaName.equals("pvr") || cinemaName.equals("PVR")) {
				cineName = new Text("Booked At: PVR CINEMAS\n\n").setFontSize(22f)
						.setTextAlignment(TextAlignment.CENTER);
			} else if (cinemaName.equals("ags") || cinemaName.equals("AGS")) {
				cineName = new Text("Booked At: AGS CINEMAS\n\n").setFontSize(22f)
						.setTextAlignment(TextAlignment.CENTER);
			}

			Text bookedBy = new Text("Booked Through: TICKETNEW\n").setFontSize(25f)
					.setTextAlignment(TextAlignment.CENTER);
			Text bookingIdText = new Text("Booking Id: \t" + bookingResponse.getBookingId() + "\n")
					.setFontSize(fontSize);

			Text movieNameText = new Text("Movie Name: \t" + bookingResponse.getMovieName() + "\n")
					.setFontSize(fontSize);

			Text movieIdText = new Text("Movie Id: \t" + bookingResponse.getMovieId() + "\n").setFontSize(fontSize);

			Text showTimeText = new Text("Movie Time: \t" + bookingResponse.getShowTime() + "\n").setFontSize(fontSize);

			Text seatsBookedText = new Text("Seats Booked: \t" + bookingResponse.getNumberOfTickets() + "\n")
					.setFontSize(fontSize);

			Text currencyText = new Text("Currency: \t" + bookingResponse.getCurrency() + "\n").setFontSize(fontSize);

			Text priceText = new Text("Price: \t" + bookingResponse.getTotalAmount() + "\n").setFontSize(fontSize);

			Text statusText = new Text("Status: \t" + bookingResponse.getStatus() + "\n").setFontSize(fontSize);

			// Add all text elements to the paragraph
			Paragraph paragraph = new Paragraph().add(confirmation).add(bookedBy).add(cineName).add(bookingIdText)
					.add(movieNameText).add(movieIdText).add(showTimeText).add(seatsBookedText).add(currencyText)
					.add(priceText).add(statusText);

			// Optionally, align text to the center
			paragraph.setTextAlignment(TextAlignment.LEFT);

			document.add(paragraph.setFontColor(new DeviceRgb(0, 0, 0)));
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outputStream.toByteArray();
	}

	public void sendEmailWithAttachment(String to, String subject, String text, byte[] pdfBytes) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(text);

			// Attach the PDF
			helper.addAttachment("booking_confirmation.pdf", new ByteArrayDataSource(pdfBytes, "application/pdf"));

			mailSender.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

private Document Document(PdfDocument pdf, PageSize a4) {
	// TODO Auto-generated method stub
	return null;
}

// Helper method to determine the cinema name
private String getCinemaName(String cinemaName) {
    if ("pvr".equalsIgnoreCase(cinemaName)) {
        return "PVR CINEMAS";
    } else if ("ags".equalsIgnoreCase(cinemaName)) {
        return "AGS CINEMAS";
    } else {
        return "UNKNOWN CINEMA";
    }
}
    private Movie getMovieById(int parseInt) {
		// TODO Auto-generated method stub
		return null;
	}

	private <T> T performGetRequest(String url, Class<T> responseType) {
        try {
            return restTemplate.getForObject(url, responseType);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to perform GET request: " + e.getMessage(), e);
        }
    }

    private <T> T performGetRequest(String url, ParameterizedTypeReference<T> responseType) {
        try {
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, null, responseType);
            return response.getBody();
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to perform GET request: " + e.getMessage(), e);
        }
    }

    private <T> T performPostRequest(String url, Object request, Class<T> responseType) {
        try {
            return restTemplate.postForObject(url, request, responseType);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to perform POST request: " + e.getMessage(), e);
        }
    }

    private User validateUser(String userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found, add user to continue"));
    }
}

