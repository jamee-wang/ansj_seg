package org.ansj.library;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ansj.app.crf.Model;
import org.ansj.app.crf.SplitWord;
import org.ansj.app.crf.model.CRFModel;
import org.ansj.dic.PathToStream;
import org.ansj.domain.KV;
import org.nlpcn.commons.lang.util.logging.Log;
import org.nlpcn.commons.lang.util.logging.LogFactory;

public class CrfLibrary  {

	private static final Log LOG = LogFactory.getLog();

	// CRF模型
	private static final Map<String, KV<String, SplitWord>> CRF = new HashMap<>();

	public static final String DEFAULT = "crf_";
	
	public static SplitWord get() {
		return get(DEFAULT);
	}

	/**
	 * 根据key获取crf分词器
	 * 
	 * @param key
	 * @return crf分词器
	 */
	public static SplitWord get(String key) {
		key = fix(key);
		KV<String, SplitWord> kv = CRF.get(key);

		if (kv == null) {
			LOG.warn("crf " + key + " not found in config ");
			return null;
		}

		SplitWord sw = (SplitWord) kv.getV();
		if (sw == null) {
			sw = initCRFModel(kv);
		}
		return sw;
	}

	/**
	 * 加载CRF模型
	 * 
	 * @param modelPath
	 * @return
	 */
	private static synchronized SplitWord initCRFModel(KV<String, SplitWord> kv) {
		try {
			if (kv.getV() != null) {
				return kv.getV();
			}

			long start = System.currentTimeMillis();
			LOG.info("begin init crf model!");
			try (InputStream is = PathToStream.stream(kv.getK())) {
				SplitWord crfSplitWord = new SplitWord(Model.load(CRFModel.class, is));
				kv.setV(crfSplitWord);
				LOG.info("load crf use time:" + (System.currentTimeMillis() - start) + " path is : " + kv.getK());
				return crfSplitWord;
			}
		} catch (Exception e) {
			LOG.error(kv + " load err " + e.getMessage());
			return null;
		}
	}

	/**
	 * 动态添加
	 * 
	 * @param dicDefault
	 * @param dicDefault2
	 * @param dic2
	 */
	public static void put(String key, String path) {
		key = fix(key);
		put(key, path, null);
	}

	public static void put(String key, String path, SplitWord sw) {
		key = fix(key);
		CRF.put(key, KV.with(path, sw));
	}

	/**
	 * 删除一个key
	 * 
	 * @param key
	 * @return
	 */
	public KV<String, SplitWord> remove(String key) {
		key = fix(key);
		return CRF.remove(key);
	}

	/**
	 * 刷新一个,将值设置为null
	 * 
	 * @param key
	 * @return
	 */
	public static void reload(String key) {
		key = fix(key);
		CRF.get(key).setV(null);
		get(key);
	}

	public static Set<String> keys() {
		return CRF.keySet();
	}

	private static String fix(String key) {
		if (key.startsWith(DEFAULT)) {
			return key;
		} else {
			return DEFAULT + key;
		}
	}

	public static void putIfAbsent(String key, String path) {
		if (!CRF.containsKey(key)) {
			CRF.put(key, KV.with(path, (SplitWord) null));
		}
	}
}