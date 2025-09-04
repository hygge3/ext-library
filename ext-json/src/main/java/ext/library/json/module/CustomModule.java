package ext.library.json.module;

import com.fasterxml.jackson.databind.module.SimpleModule;
import ext.library.json.serializer.BigDecimalPlainSerializer;
import ext.library.json.serializer.BigNumberSerializer;

import java.math.BigDecimal;
import java.math.BigInteger;

public class CustomModule extends SimpleModule {
    public CustomModule() {
        super("CustomModule");
        // BigDecimal 处理
        addSerializer(BigDecimal.class, new BigDecimalPlainSerializer());
        // 添加超出 JS 精度大数字处理
        addSerializer(Long.class, BigNumberSerializer.INSTANCE);
        addSerializer(Long.TYPE, BigNumberSerializer.INSTANCE);
        addSerializer(BigInteger.class, BigNumberSerializer.INSTANCE);
    }
}