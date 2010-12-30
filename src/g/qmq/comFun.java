package g.qmq;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.media.MediaPlayer;

public class comFun {
	public comFun() {

	}

	public boolean getAllMusicFiles(InputStream dir, boolean subFolder,
			ArrayList<HashMap<String, Object>> mFiles, boolean id3tag) {
		DocumentBuilderFactory docBuilderFactory = null;
		DocumentBuilder docBuilder = null;
		Document doc = null;
		try {
			docBuilderFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docBuilderFactory.newDocumentBuilder();
			// xml file 放到 assets目录中的
			doc = docBuilder.parse(dir);
			// root element
			Element root = doc.getDocumentElement();
			// Do something here
			// get a NodeList by tagname
			NodeList nodeList = root.getElementsByTagName("song");
			for (int i = 0; i < nodeList.getLength(); i++) {
				HashMap<String, Object> hm = new HashMap<String, Object>();

				Node nd = nodeList.item(i);

				String title = nd.getAttributes().getNamedItem("name")
						.getNodeValue();

				// Get song title.
				String id3_title = null, id3_artist = null;
				if (id3tag) {
					id3_title = nd.getAttributes().getNamedItem("id3_title")
							.getNodeValue();
					id3_artist = nd.getAttributes().getNamedItem("id3_artist")
							.getNodeValue();
				}
				if (id3_title != null && id3_artist != null) {
					hm.put("mFile_name", id3_artist + " - " + id3_title);
				} else {
					hm.put("mFile_name", title);
				}

				// Get song path.
				String path = nd.getFirstChild().getNodeValue();
				hm.put("mFile_path", path);

				mFiles.add(hm);
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			doc = null;
			docBuilder = null;
			docBuilderFactory = null;
		}
		return true;

	}

	public boolean getAllMusicFiles(String dir, boolean subFolder,
			ArrayList<HashMap<String, Object>> mFiles) {
		try{
		File files[] = new File(dir).listFiles();
		for (File file : files) { // Start seeking all files under DIR.
			HashMap<String, Object> mFile = new HashMap<String, Object>();
			// Checking permissions.
			if (!file.isHidden() && file.canRead()) {
				// If current file is a folder and search subFolders is on, go
				// dig.
				if (file.isDirectory() && subFolder)
					getAllMusicFiles(file.getPath(), subFolder, mFiles);
				// If it is not a folder and end with support music format put
				// in to
				// array.
				if (!file.isDirectory()
						&& (file.getName().endsWith(".mp3") || file.getName()
								.endsWith(".wma"))) {
					mFile.put("mFile_name", file.getName().substring(0,
							file.getName().length() - 4));
					mFile.put("mFile_path", file.getPath());
					mFiles.add(mFile);
				}
			}
		}
		}catch (Exception e){
			e.printStackTrace();
		}
		if (!mFiles.isEmpty())
			return true;
		return false;
	}

	/**
	 * 准备音乐
	 * 
	 * @param mp
	 *            MediaPlayer
	 * @param src
	 *            音乐地址
	 * @param start
	 *            开始时间定位 (如果小于0，使用默认中间位置加随机偏差)
	 * @return 准备状态. T|F
	 */
	public boolean setMusic(MediaPlayer mp, boolean isLoop, String src,
			int start) {
		try {
			if (mp.isPlaying()) {
				mp.stop();
				mp.reset();
			}
			mp.setDataSource(src);
			mp.prepare();
			if (start < 0) {
				int mLength = mp.getDuration();
				if (mLength < 1) {
					start = 0;
				} else {
					Random rng = new Random();
					start = (int) ((mLength / 2) + (Math
							.pow(-1, rng.nextInt(1)) * (mLength / 10)));
				}
			}
			mp.seekTo(start);
			mp.setLooping(isLoop);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			if (mp.isPlaying())
				mp.stop();
			mp.reset();
			return false;
		} catch (IllegalStateException e) {
			e.printStackTrace();
			if (mp.isPlaying())
				mp.stop();
			mp.reset();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			if (mp.isPlaying())
				mp.stop();
			mp.reset();
			return false;
		}
		return true;
	}

}