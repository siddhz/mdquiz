package g.qmq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class webToolBox {
	

	public webToolBox() {
	}
	/**
	 * 建立Post网络连接
	 * @param url string 连接地址
	 * @param cont String[] Post内容(名称,值间隔加入)
	 * @return 是否成功获取内容
	 */
	public boolean webCon(String url, String[] cont) {
		/* 建立HTTPost对象 */
		HttpPost httpRequest = new HttpPost(url);
		/* NameValuePair实现请求参数的封装 */
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		//加入安全码
		params.add(new BasicNameValuePair("sCode", sCode));
		//加入POST参数
		for(int i=0,tSize=cont.length;i<tSize-1;i++){
			params.add(new BasicNameValuePair(cont[i++],cont[i]));
			Log.v("POST", cont[i-1]);
			Log.v("POST", cont[i]);
		}
		try {
			/* 添加请求参数到请求对象 */
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			/* 发送请求并等待响应 */
			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(httpRequest);
			/* 若状态码为200 ok */
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				/* 读返回数据(整个Post后的页面源码.) */
				strResult = EntityUtils.toString(httpResponse.getEntity(), "utf_8");
				Log.v("WebReturn", strResult);
				return true;
			} else {
				errMsg = "Error: "
						+ httpResponse.getStatusLine().toString();
				return false;
			}
		} catch (ClientProtocolException e) {
			errMsg = e.getMessage().toString();
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			errMsg = e.getMessage().toString();
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			errMsg = e.getMessage().toString();
			e.printStackTrace();
			return false;
		}
	}
	public String errMsg = null, strResult;
	public int myRank;
	
	private final static String sCode = "9EB2F1619156C86DD91C5652516F175E";
}
