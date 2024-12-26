/*
 * Copyright (c) 2019-2029, Dreamlu 卢春梦 (596392912@qq.com & www.dreamlu.net).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ext.library.web.validation.validator;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ext.library.web.validation.constraints.RangeIn;
import ext.library.tool.$;
import org.springframework.util.StringUtils;

/**
 * 范围验证
 *
 * @author dy
 */
public class RangeInValidator implements ConstraintValidator<RangeIn, Object> {

	private RangeIn rangeIn;

	@Override
	public void initialize(RangeIn constraintAnnotation) {
		this.rangeIn = constraintAnnotation;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		// 如果 value 为空则不进行验证，为空验证可以使用 @NotBlank @NotNull @NotEmpty 等注解来进行控制，职责分离
		if (value == null) {
			return true;
		}
		String[] ranges = StringUtils.delimitedListToStringArray(rangeIn.value(), ",", " \t\n\n\f");
		switch (value) {
			case CharSequence obj -> {
				return $.contains(ranges, obj);
			}
			case Number obj -> {
				return $.contains(ranges, obj.toString());
			}
			case Collection obj -> {
				return obj.stream().allMatch(it -> $.contains(ranges, it.toString()));
			}
			case Iterable obj -> {
				AtomicBoolean flag = new AtomicBoolean(true);
				obj.forEach(it -> {
					if (!$.contains(ranges, it.toString())) {
						flag.set(false);
					}
				});
				return flag.get();
			}
			case Object[] obj -> {
				return Arrays.stream(obj).allMatch(it -> $.contains(ranges, it.toString()));
			}
			default -> {
				return false;
			}
		}
	}

}
