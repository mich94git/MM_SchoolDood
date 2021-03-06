

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import entities.User;

/**
 * Servlet implementation class ServletAbstract
 */
@WebServlet("/ServletAbstract")
public class ServletAbstract extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ServletAbstract() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	protected void displayLayout(String jsp, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.displayLayout(jsp, request, response, "");
	}
	
	protected void displayLayout(String jsp, HttpServletRequest request, HttpServletResponse response, String messageFlash) throws ServletException, IOException {
		if(messageFlash != "") {
			request.setAttribute("messageFlash", true);
			request.setAttribute("flash", messageFlash);
		}
		
		this.getServletContext().getRequestDispatcher("/WEB-INF/header.jsp").include(request, response);
		this.getServletContext().getRequestDispatcher(jsp).include(request, response);
		this.getServletContext().getRequestDispatcher("/WEB-INF/footer.jsp").include(request, response);
	}
	
	protected User getCurrentUser(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(true);
		
		return (User) session.getAttribute("user");
	}

	protected boolean accessControl(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(true);
		
		if (session.getAttribute("logged") == null) {

			try {
				this.displayLayout("/WEB-INF/Home/index.jsp", request, response);
				return false;
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		
		return true;
	}
}
