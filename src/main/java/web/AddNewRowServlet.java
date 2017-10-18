package web;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import db.Table;
import dbtype.Attribute;

/**
 * Servlet implementation class AddNewRowServlet
 */
public class AddNewRowServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddNewRowServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) {
    	String inputLine;
		ArrayList<String> values = new ArrayList<>();
    	try (BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
    		while ((inputLine = br.readLine()) != null) {
    			values.add(inputLine);
    		}
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    	
		String tableName = request.getParameter("tableName");
		
		Table table = null;
		try {
			table = Common.server.tableRequest(tableName);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ArrayList<Attribute> attributes = new ArrayList<>(values.size());
		for (int i = 0; i < values.size(); i++) {
			try {
				attributes.add(table.constructField(i, values.get(i)));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			Common.server.addNewRow(tableName, attributes);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handleRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handleRequest(request, response);
	}

}
