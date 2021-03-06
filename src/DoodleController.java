

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import entities.Answers;
import entities.CategoryAnswer;
import entities.CategoryAnswers;
import entities.Date;
import entities.Dates;
import entities.Doodle;
import entities.Emails;
import entities.User;
import librairies.SendHTMLEmail;
import models.AnswerRepository;
import models.CategoryAnswerRepository;
import models.DateRepository;
import models.DoodleRepository;
import models.EmailRepository;

/**
 * Servlet implementation class Doodle
 */
@WebServlet("/Doodle")
public class DoodleController extends ServletAbstract {
	private static final long serialVersionUID = 1L;
		
	private CategoryAnswerRepository answer_categories;
	private EmailRepository emails;
	private DoodleRepository doodle;
	private AnswerRepository answer;
	private DateRepository date;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DoodleController() {
        super();
        
        this.emails = new EmailRepository();
		this.answer_categories = new CategoryAnswerRepository();
		this.doodle = new DoodleRepository();
		this.answer = new AnswerRepository();
		this.date = new DateRepository();
    }
  
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		if (request.getParameter("did") != null) {
			Doodle doodle = this.doodle.findById(Integer.parseInt(request.getParameter("did")));
			
			if (doodle == null) {		
				super.displayLayout("/WEB-INF/Home/index.jsp", request, response);
				return;
			}
			
			Answers answers = this.answer.findAllByCatId(doodle.getCaid());
			Dates dates = this.date.findAllByDoodleId(doodle.getDid());
			
			request.setAttribute("doodle", doodle);
			request.setAttribute("answers", answers);
			request.setAttribute("dates", dates);
			
			if (doodle.getStatus() == 0) {
				super.displayLayout("/WEB-INF/Doodle/show.jsp", request, response);
				return;
			}
			
			if (doodle.getStatus() == 1) {
				if (request.getParameter("token") != null && doodle.getToken().compareTo(request.getParameter("token")) == 0){
					super.displayLayout("/WEB-INF/Doodle/show.jsp", request, response);
					return;
				}
				else{
					super.displayLayout("/WEB-INF/Home/index.jsp", request, response, "Ce doodle est priv� et vous n'y avez pas acc�s.");
					return;
				}
			}
			
			super.displayLayout("/WEB-INF/Home/index.jsp", request, response, "Statut invalide");
			return;
		}
		
		if (!super.accessControl(request, response)) {
			return;
		}
				
		CategoryAnswers categories = this.answer_categories.findAll();
		Emails emails = this.emails.findAllByUserId(super.getCurrentUser(request, response).getUid());
		
		request.setAttribute("categories", categories);
		request.setAttribute("emails", emails);
		
		if (request.getAttribute("message") != null) {
			System.out.println((String) request.getAttribute("message"));
			
			super.displayLayout("/WEB-INF/Doodle/index.jsp", request, response, (String) request.getAttribute("message"));			
		}
		else {
			super.displayLayout("/WEB-INF/Doodle/index.jsp", request, response);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Enumeration paramNames = request.getParameterNames();

        int status = 1;
        String checkbox = (String) request.getParameter("status");
        
        if (checkbox == null || checkbox.compareTo("off") == 0) {
        	status = 0;
        }
       
		String[] emailsParams = request.getParameterValues("emails[]");
		
        if (emailsParams == null){
        	request.setAttribute("message",  "Vous devez inviter au moins une personne.");
        	this.doGet(request, response);
        	return;
        }
        
        String question = (String) request.getParameter("question");
		
        int id = this.doodle.save(super.getCurrentUser(request, response).getUid(), Integer.parseInt(request.getParameter("caid")), status, question);

        if (id != 0) {
            Dates dates = new Dates();
            
            String[] hours = request.getParameterValues("datetime[hour][]");
            String[] minutes = request.getParameterValues("datetime[min][]");
            String[] date_inputs = request.getParameterValues("datetime[date][]");
                    
            Date d = new Date();
             
            for(int i=0; i<hours.length; i++) 
            {
            	d = new Date(id, hours[i] + ':' + minutes[i], date_inputs[i]);
            	dates.push(d);
            }        	
        	
        	Doodle doodle = this.doodle.findById(id);
        	
        	if (!this.date.saveAll(dates)) {
            	request.setAttribute("message",  "Doodle sauvegard� mais une ou plusieurs dates sont invalides");        		
        	}
        	
        	String url = request.getRequestURL().toString() + "?did=" + String.valueOf(doodle.getDid()) + "&token=" + doodle.getToken();
        	System.out.println(url);
    		SendHTMLEmail mailer = new SendHTMLEmail();
        	for (int i = 0 ; i < emailsParams.length ; i++) {
        		String content = "<html>"
        				+ "<head><title></title>"
        				+ "</head>"
        				+ "<body>"
        				+ "<h1>" + question + "</h1>"
        				+ "<p>Vous venez de recevoir un doodle ! R�pondez � cette adresse : "
        				+ "<a href=\"" 
        				+ url
        				+ "\">"
        				+ url
        				+ "</a>"
        				+ "<p>A bient�t ! </p>"
        				+ "</body>"
        				+ "</html>";
        		mailer.sendMail(emailsParams[i], content);
        	}
        	
        	request.setAttribute("message", "Doodle ajout� !");
			
			this.doGet(request, response);
        }
        else {
        	super.displayLayout("/WEB-INF/Doodle/index.jsp", request, response, "Impossible d'ajout� le doodle ! :(");
        }
	}

}
