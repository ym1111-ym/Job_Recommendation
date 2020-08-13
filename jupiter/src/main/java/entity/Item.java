package entity;

import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;


public class Item {
	private String itemId;
	private String name;
	private String address;
	private Set<String> keywords;
	private String imageUrl;
	private String url;
	
//因为我们没有setter 所以constructor很重要
//	public Item(String itemId, String name, String address, Set<String> keywords, String imageUrl, String url) {
//		super();
//		this.itemId = itemId;
//		this.name = name;
//		this.address = address;
//		this.keywords = keywords;
//		this.imageUrl = imageUrl;
//		this.url = url;
//	}
	
	private Item(ItemBuilder builder) {
		this.itemId = builder.itemId;
		this.name = builder.name;
		this.address = builder.address;
		this.imageUrl = builder.imageUrl;
		this.url = builder.url;
		this.keywords = builder.keywords;
	}
	//we don't need setters, because we don't need to change the variable after initialization, we will use what Github give us
	
	public String getItemId() {
		return itemId;
	}
	public String getName() {
		return name;
	}
	public String getAddress() {
		return address;
	}
	public Set<String> getKeywords() {
		return keywords;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public String getUrl() {
		return url;
	}
	
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		obj.put("item_id", itemId);
		obj.put("name", name);
		obj.put("address", address);
		obj.put("keywords", new JSONArray(keywords));
		obj.put("image_url", imageUrl);
		obj.put("url", url);
		return obj;
	}

	
	/*
	 * 比起上面的这样比较好 因为如果我们要construct一个item 也许想pass进来不同的parameter
	   Item test= new Item(itemId); OR Item test= new Item(itemId, name)等等
	   builder pattern 是一种design pattern
	   必须是内部类 and static 类
	   static because if 不是 static 为need先new一个item 我们要有item 必须用builder给它build出来 鸡生蛋问题 
	   so must be static 
	   
	   现在create new item就比较灵活了
	   ItemBuild builder = new ItemBuilder();
	   builder.setItemId(01);
	   builder.setName("Isabel"); 想set哪些variable 就可以set哪些
	   Item item = builder.build();
    */
	
	public static class ItemBuilder {
		private String itemId;
		private String name;
		private String address;
		private String imageUrl;
		private String url;
		private Set<String> keywords;
		
		public void setItemId(String itemId) {
			this.itemId = itemId;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public void setKeywords(Set<String> keywords) {
			this.keywords = keywords;
		}
		
		public Item build() {
			return new Item(this);
		}
	}
}
