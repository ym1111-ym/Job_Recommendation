package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import entity.Item;
import entity.Item.ItemBuilder;

/*
 * 这里是一些API 从数据库拿到我们favorite的job items
 */
public class MySQLConnection {
	private Connection conn;
	public MySQLConnection() {
		try {
			/*mysqltablecreation 用过 帮你建立一个和 MySQL 的链接
			 * https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-usagenotes-connect-drivermanager.html
			 * 上面官网可以找到code
			 */
			 
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * 给用户存favorite，存在history里面
	 */
	public void setFavoriteItems(String userId, Item item) {
		//数据库没连上 直接return
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		
		saveItem(item);//需要save 因为itemid是primary 如果 不存在 sql会觉得他是违法操作
		
		//看下history table的创建 有三个variable 还有个timestamp，所以要把两个写出来
		//这是JDBC specific的，如果用第一个 就要用JDBC专属的reparedStatement and setString 这个function
		String sql = "INSERT INTO history (user_id, item_id) VALUES (?, ?)";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);      //index 从1开始
			statement.setString(2, item.getItemId());
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		/*String sql =String.format("INSERT INTO history(user_id, item_id) VALUES(%s, %s"), userId, Item.GetItemId());
		 * try{
		 * 	Statement statement = conn.createStatement();
		 * statement.executeUpdate(sql);
		 * }catch(SQLException e){
		 * 	e.printStackTrace();
		 * }
		* 这个更general 
		*/
		
	}
	
	//set时候提供item， set时候提供itemId，insert into history, we need pass item as a parameter, because we need 
	//delete的时候可以通过 item ID 知道是要删除那一个
	public void unsetFavoriteItems(String userId, String itemId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
		//问好的好处 把输入进来的input当成一个整体，恶意输入就没办法对我们的database做什么破坏
		            
		try {
			PreparedStatement statement = conn.prepareStatement(sql);//用问号 就得用JDBC library里的PreparedStatement
			statement.setString(1, userId);
			statement.setString(2, itemId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/*
	 * 这里用不用把item delete呢？如果只有我一个人收藏了这个job 我取消后 没人收藏了 就变垃圾了
	 * 可以删，也可以不删，如果还有其他用户 肯定不删
	 * 可以定期gc 实时操作很在乎效率 如果这时候清空会影响用户使用体验
	 */

	public void saveItem(Item item) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?)";
		/*因为item有5 column, so we don't need to specify, just use ?
		 * sql 一旦发现duplicate 的情况 不会报错 而是会忽略，如果不加ignore，有duplicate会报错
		 */
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			statement.setString(2, item.getName());
			statement.setString(3, item.getAddress());
			statement.setString(4, item.getImageUrl());
			statement.setString(5, item.getUrl());
			statement.executeUpdate();
			
			/*
			 * 也得更新keyword
			 */
			sql = "INSERT IGNORE INTO keywords VALUES (?, ?)";
                    statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			/*
			 * 
			 */
			for (String keyword : item.getKeywords()) {
				statement.setString(2, keyword);
				statement.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/*当我们想看我们的user 收藏了哪些job
	 * first we need to get the item id for this user from the history table
	 * then use these itemid to find full information from the item table
	 */
	public Set<String> getFavoriteItemIds(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
		}

		Set<String> favoriteItems = new HashSet<>();

		try {
			String sql = "SELECT item_id FROM history WHERE user_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			//之前是写操作 所以用executeupdate 
			//现在我们是读操作 需要储存从数据库读回来的结果，用resutset， executeQuery
			//借来把resultset的结果读出来， resultset可以理解为一个表格，列就是select的column
			//rs.next 是resultset默认的iterator
			while (rs.next()) {
				String itemId = rs.getString("item_id");//里面放的是column的名字
				favoriteItems.add(itemId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteItems;
	}
	
	public Set<Item> getFavoriteItems(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
		}
		Set<Item> favoriteItems = new HashSet<>();
		//step 1: get favorite IDs
		Set<String> favoriteItemIds = getFavoriteItemIds(userId);
		
		//step 2: get favorite itemds based on the ID
		//注意 上面getFavoriteItemIds 是从history table 通过userID找
		//这里getFavoriteItems 是从 items table通过item_id找
		String sql = "SELECT * FROM items WHERE item_id = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			for (String itemId : favoriteItemIds) {
				statement.setString(1, itemId);
				ResultSet rs = statement.executeQuery();

				ItemBuilder builder = new ItemBuilder();
				if (rs.next()) {
					builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					builder.setKeywords(getKeywords(itemId));
					favoriteItems.add(builder.build());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteItems;
	}
	
	public Set<String> getKeywords(String itemId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return null;
		}
		Set<String> keywords = new HashSet<>();
		String sql = "SELECT keyword from keywords WHERE item_id = ? ";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, itemId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String keyword = rs.getString("keyword");
				keywords.add(keyword);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return keywords;
	}
	
	// class 15 的内容 authentication 
	public String getFullname(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return "";
		}
		String name = "";
		String sql = "SELECT first_name, last_name FROM users WHERE user_id = ? ";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			//1 代表第一个问号 注意 不是以零为base
			ResultSet rs = statement.executeQuery();
			//上一行就是执行这个query
			if (rs.next()) {//如果找到了 返回true
				name = rs.getString("first_name") + " " + rs.getString("last_name");
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return name;
	}
	
	//也可以分开做validation 比如返回没有这个user or 密码不对 但在这里是一起做的
	public boolean verifyLogin(String userId, String password) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		String sql = "SELECT user_id FROM users WHERE user_id = ? AND password = ?";
		                                                    //  1                2                                
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return false;
	}
	
	public boolean addUser(String userId, String password, String firstname, String lastname) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}

		String sql = "INSERT IGNORE INTO users VALUES (?, ?, ?, ?)";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			statement.setString(3, firstname);
			statement.setString(4, lastname);
			//这里和MySQLTableCreation都是一一对应的
			
			return statement.executeUpdate() == 1;
			/*这里会return 被更新的row的数量 or 0 if nothing is updated
			 * 这里只想要更新一行
			 */
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
}


