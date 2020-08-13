package external;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.monkeylearn.ExtraParam;
import com.monkeylearn.MonkeyLearn;
import com.monkeylearn.MonkeyLearnException;
import com.monkeylearn.MonkeyLearnResponse;

public class MonkeyLearnClient {
	private static final String API_KEY = "4790b691b2e6523f6a3fc55af28eac63b85226fe";
	
	//main函数是用来做test的
	public static void main(String[] args) {
		String[] textList = {
				"Elon Musk has shared a photo of the spacesuit designed by SpaceX. "
				+ "This is the second image shared of the new design and the first "
				+ "to feature the spacesuit’s full-body look.", };
		List<List<String>> words = extractKeywords(textList);
		for (List<String> ws : words) {
			for (String w : ws) {
				System.out.println(w);
			}
			System.out.println();
		}
	}

	public static List<List<String>> extractKeywords(String[] text) {
		if (text == null || text.length == 0) {
			return new ArrayList<>();
		}
		MonkeyLearn ml = new MonkeyLearn(API_KEY);
		ExtraParam[] extraParams = { new ExtraParam("max_keywords", "3") }; //customization 只return top 3
		MonkeyLearnResponse response;
		try {
			response = ml.extractors.extract("ex_YCya9nrn", text, extraParams);// that's the model ID
			JSONArray resultArray = response.arrayResult;
			return getKeywords(resultArray);
		} catch (MonkeyLearnException e) {// it’s likely to have an exception
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	/*
	 * transform 成一个list of list的结果 “hello world” "black live matter"
	 * "front end developer" is transformed to
	 * 
	 * "hello", "world" "black", "live", "matters" "front", "end", "developer"
	 */
	
	//把json转换成list of list
	private static List<List<String>> getKeywords(JSONArray mlResultArray) {
		// return的结果
		List<List<String>> topKeywords = new ArrayList<>();
		// Iterate the result array and convert it to our format.
		for (int i = 0; i < mlResultArray.size(); ++i) { // scan every line
			List<String> keywords = new ArrayList<>();
			JSONArray keywordsArray = (JSONArray) mlResultArray.get(i);
			for (int j = 0; j < keywordsArray.size(); ++j) { // scan every word
				JSONObject keywordObject = (JSONObject) keywordsArray.get(j);
				// We just need the keyword, excluding other fields.
				String keyword = (String) keywordObject.get("keyword");
				keywords.add(keyword); // 把每个单词放进去

			}
			topKeywords.add(keywords); // 放进要返回的list里面
		}
		return topKeywords;
	}
}
