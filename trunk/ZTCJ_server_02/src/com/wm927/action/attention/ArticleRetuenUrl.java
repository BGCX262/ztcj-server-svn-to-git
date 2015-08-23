package com.wm927.action.attention;

public class ArticleRetuenUrl {
	
	public static  String GetUrlByChannelId(String channelid,String id) {
		String url = "";
        if (channelid.equals("2")) {
            url = "http://wm927.com/news/" + id;
        }
        else if (channelid.equals("8")){
            url = "http://wm927.com/expert/" + id;
        }
        else if (channelid.equals("6")) {
            url = "http://wm927.com/economic/" + id;
        }
        else if (channelid.equals("10")) {
            url = "http://wm927.com/currency/" + id;
        }
        else if (channelid.equals("4")) {
            url = "http://wm927.com/gold/" + id;
        }
        else if (channelid.equals("12")){
            url = "http://wm927.com/dowjones/" + id;
        }
        else{
            url = "http://wm927.com/news/" + id;
        }
        return url;
    }

	
}
