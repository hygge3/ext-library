package ext.library.translation.service.impl;

import ext.library.translation.annotation.TranslationType;
import ext.library.translation.constant.TransConstant;
import ext.library.translation.service.TranslationInterface;

/**
 * 字典翻译实现
 */
@TranslationType(type = TransConstant.DICT_TYPE_TO_LABEL)
public class DictTypeTranslationImpl implements TranslationInterface<String> {

	@Override
	public String translation(Object key, String other) {
		if (key instanceof String dictValue && other != null && !other.isEmpty()) {
			// do something
			return "";
		}
		return null;
	}

}