package rpc;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import db.MySQLConnection;
import entity.Item;
import external.GitHubClient;

/**
 * Servlet implementation class SearchItem
 */
public class SearchItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchItem() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		response.setContentType("application/json");
//		PrintWriter writer = response.getWriter();
//		//only return if we get the parameter:username
//		if (request.getParameter("username") != null) {
//			JSONObject obj = new JSONObject();
//			String username = request.getParameter("username");
//			obj.put("username", username);
//			writer.print(obj);
//		}
//		/*网页测试：http://localhost:8080/jupiter/SearchItem
//		改成： http://localhost:8080/jupiter/SearchItem?username=isabel
//		类似validation的过程
//		username=isabel 这是一个query 给它一个参数
//		
//		*/
		
//		response.setContentType("application/json");
//		PrintWriter writer = response.getWriter();
//		
//		JSONArray array = new JSONArray();
//		array.put(new JSONObject().put("username", "abcd"));
//		array.put(new JSONObject().put("username", "1234"));
//		writer.print(array);
		
		/*这里要达到的效果是 search完了返回的结果 有些是我们收藏过的
		 * 有些是没有收藏的 这个决定有没有实心的heart icon
		 * 所以需要做一个处理
		 */
		
		HttpSession session = request.getSession(false);//check session is valid
		if (session == null) {
			response.setStatus(403);
			return;
		}
		
		String userId = request.getParameter("user_id");

		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));

		GitHubClient client = new GitHubClient();
		List<Item> items = client.search(lat, lon, null);//search返回的事list of item
		
		MySQLConnection connection = new MySQLConnection();
		Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);
		connection.close();


		JSONArray array = new JSONArray(); // create a json array
//		for (Item item : items) {//iterate every item in the item array
//			array.put(item.toJSONObject()); //convert every item into a json object 
//		}
		
		for (Item item : items) {
			JSONObject obj = item.toJSONObject();
			obj.put("favorite", favoritedItemIds.contains(item.getItemId()));
			array.put(obj);
		}

		RpcHelper.writeJsonArray(response, array);
		/*转会json array 等于是 我们从github拿到json格式的信息 做一些处理 留下我们想要的信息
		 * 形成item 返回的时候再转会json object
		 * 加进json array
		 */
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
