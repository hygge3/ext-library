package ext.library.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * 签名算法类型
 */
@Getter
@AllArgsConstructor
public enum SignType {

	/**
	 * 一般用于沙箱环境
	 */
	MD5("MD5"),
	/**
	 * 一般用于正式环境
	 */
	HMAC_SHA256("HMAC-SHA256");

	private final String str;

	@Nullable
	public static SignType of(String str) {
		for (SignType e : values()) {
			if (e.str.equals(str)) {
				return e;
			}
		}
		return null;
	}

}
