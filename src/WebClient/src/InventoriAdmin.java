
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kelas.Barang;
import kelas.Database;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.postgresql.Driver;

import DefaultNamespace.HelloProxy;

/**
 * Servlet implementation class DetailBarang
 */
public class InventoriAdmin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String DB_NAME = "toko_imba";
	static final String helloService = Database.WebServiceURL + "services/Hello";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public InventoriAdmin() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		int barangId = -1; 
		boolean add = true;
		String action = "";
		try{
			action = request.getParameter("action");
			request.setAttribute("action", action);
			
			add = action.equals("add");
			
			barangId = Integer.parseInt(request.getParameter("gid"));						
			request.setAttribute("gid", Integer.toString(barangId));
			
		} catch(Exception e){}
		
		Barang barang = null;
		
		if (add){
			barang = new Barang("");	
			request.setAttribute("barang", barang);
		}else{
			
			JSONObject jsonResponse = null;
			
			JSONObject data = new JSONObject();
			data.put("action", "detail");
			data.put("gid", Integer.valueOf(request.getParameter("gid")));
			
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost(Database.WebServiceURL + "Actions");
			
			httppost.setEntity(new StringEntity(data.toString()));
			CloseableHttpResponse httpresp = httpclient.execute(httppost);
			try {
				HttpEntity entity = httpresp.getEntity();
			    if (entity != null) {
			    	String jsonresp = EntityUtils.toString(entity);
		            jsonResponse = (JSONObject) JSONValue.parse(jsonresp);
			    }
			} finally {
			    httpresp.close();
			    httpclient.close();
			}
			
			JSONObject tmp = (JSONObject) jsonResponse.get("data");
			String name = (String) tmp.get("nama_inventori");
			barang = new Barang(name);
			barang.setId_cat(((Long) tmp.get("id_kategori")).intValue());
			barang.setId_inven(((Long) tmp.get("id_inventori")).intValue());
			barang.setDesc((String) tmp.get("description"));
			barang.setHarga(((Long) tmp.get("harga")).intValue());
			barang.setGambar((String) tmp.get("gambar"));
			barang.setJumlah(((Long) tmp.get("jumlah")).intValue());
			
			request.setAttribute("barang", barang);
		}
		
		request.getRequestDispatcher("inventori.jsp").forward(request, response);		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		if(!ServletFileUpload.isMultipartContent(request)){
			response.getWriter().write("Non-multipart");
			return;			
		}
		
		try{
			
			List<FileItem> multiparts = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
			String action = ""; int id = -1;
			Barang barang = new Barang("");
			
			for (FileItem item : multiparts) {
			    if (item.isFormField()) {
			        String key = item.getFieldName();
			        String value = item.getString();
			        
			        if(key.equals("action")) action = value;
			        else if(key.equals("gid")) id = Integer.parseInt(value);
			        else if(key.equals("nama")) barang.setNama(value);
			        else if(key.equals("harga")) barang.setHarga(Integer.parseInt(value));
			        else if(key.equals("jumlah")) barang.setJumlah(Integer.parseInt(value));
			        else if(key.equals("kategori")) barang.setId_cat(Integer.parseInt(value));
			        else if(key.equals("desc")) barang.setDesc(value);
			    }else{
			        barang.setGambar(item.getName());
			    }
			}
		;
		
			Driver driv = new Driver();
			
			java.sql.Connection con = null;
			Class.forName("org.gjt.mm.mysql.Driver");
			con = driv.connect("jdbc:postgresql://ec2-107-22-234-129.compute-1.amazonaws.com:5432/dd5q059l0v49cm?user=igsiblnhyllajh&password=aFEyJCyJ4bES-kRZV_bKZrCI6f&ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory", null);
			System.out.println (DB_NAME+ "database successfully opened.");
			
			Statement statement = con.createStatement();
			
			System.out.println("tes1");
			if(action.equals("add")){
				id = add(multiparts, statement, barang);
				System.out.println("tes2");
			}else if(action.equals("edit")){
				edit(id, barang);
			}else if(action.equals("delete")){
				delete(statement, id);
			}
			
			if(action.equals("delete"))
				response.sendRedirect("");
			else
				response.sendRedirect("inventori?action=edit&gid=" + Integer.toString(id));
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	private static String quote(String str){
		return new StringBuilder().append("'").append(str).append("'").toString();
	}
	
	private static void copyFile(File src, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(src);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }
	
	private void syncTemp(int id){
		try{
			String src = Database.uploadDir + id + ".jpg";
			String dest = getServletContext().getRealPath("/res/barang/" + id + ".jpg");
			copyFile(new File(src), new File(dest));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void copyDefault(int id){
		try{
			String src = Database.uploadDir + "default.jpg";
			String dest = Database.uploadDir + id + ".jpg";
			copyFile(new File(src), new File(dest));
			
			syncTemp(id);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void uploadImg(List<FileItem> multiparts, int id){
	    try{              
	        for(FileItem item : multiparts){
	            if(!item.isFormField()){
	                item.write( new File(Database.uploadDir + id + ".jpg"));
	            }
	        }
	       System.out.println("File Uploaded Successfully");
	       syncTemp(id);
	    } catch (Exception ex) {
	    	System.out.println("File Upload Failed due to " + ex);
	    }
	}
	
	private int add(List<FileItem> multiparts, Statement statement, Barang barang) throws SQLException{
		StringBuilder query = new StringBuilder();
				
		query.append("INSERT INTO inventori(id_kategori, nama_inventori, jumlah, gambar, description, harga) VALUES (");
		query.append(barang.getId_cat()).append(",");
		query.append(quote(barang.getNama())).append(",");
		query.append(barang.getJumlah()).append(",");
		query.append(quote("")).append(",");
		query.append(quote(barang.getDesc())).append(",");
		query.append(barang.getHarga()).append(")");
		
		
		//statement.executeUpdate(query.toString());
		//PENGGANTI ATAS
		HelloProxy HePro = new HelloProxy(helloService);
		
		try{
			HePro.insertBarang(query.toString());
		}catch(Exception e){
			e.printStackTrace();
		}
		
		// get id
		
		ResultSet rs = statement.executeQuery("SELECT * FROM inventori ORDER BY id_inventori DESC");
		
		rs.next();
			barang.setId_inven(rs.getInt("id_inventori"));
		
		
		System.out.println("id :" + barang.getId_inven());
		
		System.out.println("field: " + barang.getFieldGambar());
		
		if(barang.getFieldGambar().equals("")){
			copyDefault(barang.getId_inven());
		}else{
			uploadImg(multiparts, barang.getId_inven());
		}
		
		
		return barang.getId_inven();
	}
	
	private void edit(int id, Barang barang) throws SQLException, ClientProtocolException, IOException{
		System.out.println("Masuk sini lho");
		
		JSONObject jsonResponse = null;
		
		JSONObject data = new JSONObject();
		data.put("action", "edit_barang");
		
		data.put("id_kategori", new Integer(barang.getId_cat()));
		data.put("nama_inventori", barang.getNama());
		data.put("jumlah", new Integer(barang.getJumlah()));
		data.put("description", barang.getDesc());
		data.put("harga", new Integer(barang.getHarga()));
		data.put("id_inventori", new Integer(id));
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(Database.WebServiceURL + "Actions");
		CloseableHttpResponse httpresp = null;
		
		httppost.setEntity(new StringEntity(data.toString()));
		httpresp = httpclient.execute(httppost);
		HttpEntity entity = httpresp.getEntity();
	    if (entity != null) {
	    	String jsonresp = EntityUtils.toString(entity);
            jsonResponse = (JSONObject) JSONValue.parse(jsonresp);
	    }
	    httpresp.close();
	    httpclient.close();
	}
	
	private void deleteFile(int id){
		try{
			File permanent = new File(Database.uploadDir + id + ".jpg");
			File temp = new File(getServletContext().getRealPath("/res/barang/" + id + ".jpg"));
			
			permanent.delete(); temp.delete();			
		}catch(Exception e){
			e.printStackTrace();
		}		
	}
	
	private void delete(Statement statement, int id) throws SQLException, ClientProtocolException, IOException{
		JSONObject jsonResponse = null;
		
		JSONObject data = new JSONObject();
		data.put("action", "delete_barang");
		
		data.put("id", new Integer(id));
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(Database.WebServiceURL + "Actions");
		CloseableHttpResponse httpresp = null;
		
		httppost.setEntity(new StringEntity(data.toString()));
		httpresp = httpclient.execute(httppost);
		HttpEntity entity = httpresp.getEntity();
	    if (entity != null) {
	    	String jsonresp = EntityUtils.toString(entity);
            jsonResponse = (JSONObject) JSONValue.parse(jsonresp);
	    }
	    httpresp.close();
	    httpclient.close();
	}

}
