package ext.library.json.util;

import ext.library.tool.core.Exceptions;
import ext.library.tool.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import jakarta.annotation.Nullable;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.io.StringReader;

/**
 * xpath 解析 xml
 * <p>
 * <a href="http://www.w3school.com.cn/xpath/index.asp">文档地址</a>
 * </p>
 */
public class XmlUtil {

    private final XPath path;

    private final Document doc;

    private XmlUtil(InputSource inputSource, boolean unsafe) throws Exception {
        DocumentBuilderFactory dbf = unsafe ? XmlUtil.getUnsafeDocumentBuilderFactory()
                : XmlUtil.getDocumentBuilderFactory();
        DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
        doc = documentBuilder.parse(inputSource);
        path = XmlUtil.getXpathFactory().newXPath();
    }

    private static XmlUtil createSafe(InputSource inputSource) {
        return create(inputSource, false);
    }

    private static XmlUtil createUnsafe(InputSource inputSource) {
        return create(inputSource, true);
    }

    private static XmlUtil create(InputSource inputSource, boolean unsafe) {
        try {
            return new XmlUtil(inputSource, unsafe);
        } catch (Exception e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 构造 XmlHelper，安全模式
     *
     * @param is InputStream
     *
     * @return XmlHelper
     */
    public static XmlUtil safe(InputStream is) {
        InputSource inputSource = new InputSource(is);
        return createSafe(inputSource);
    }

    /**
     * 构造 XmlHelper，安全模式
     *
     * @param xmlStr xml 字符串
     *
     * @return XmlHelper
     */
    public static XmlUtil safe(String xmlStr) {
        try (StringReader sr = new StringReader(xmlStr.trim())) {
            InputSource inputSource = new InputSource(sr);
            return XmlUtil.createSafe(inputSource);
        }
    }

    /**
     * 构造 XmlHelper，非安全模式
     *
     * @param is InputStream
     *
     * @return XmlHelper
     */
    public static XmlUtil unsafe(InputStream is) {
        InputSource inputSource = new InputSource(is);
        return createUnsafe(inputSource);
    }

    /**
     * 构造 XmlHelper，非安全模式
     *
     * @param xmlStr xml 字符串
     *
     * @return XmlHelper
     */
    public static XmlUtil unsafe(String xmlStr) {
        try (StringReader sr = new StringReader(xmlStr.trim())) {
            InputSource inputSource = new InputSource(sr);
            return XmlUtil.createUnsafe(inputSource);
        }
    }

    private static DocumentBuilderFactory getDocumentBuilderFactory() {
        return XmlHelperHolder.DOCUMENT_BUILDER_FACTORY;
    }

    /**
     * 不安全的 Document 构造器，用来解析部分可靠的 html、xml
     *
     * @return DocumentBuilderFactory
     */
    private static DocumentBuilderFactory getUnsafeDocumentBuilderFactory() {
        return XmlHelperHolder.UNSAFE_DOCUMENT_BUILDER_FACTORY;
    }

    private static XPathFactory getXpathFactory() {
        return XmlHelperHolder.XPATH_FACTORY;
    }

    /**
     * 执行 xpath 语法
     *
     * @param expression xpath 语法
     * @param item       子节点
     * @param returnType 返回的类型
     *
     * @return {Object}
     */
    public Object evalXPath(String expression, @Nullable Object item, QName returnType) {
        item = ObjectUtil.defaultIfNull(item, doc);
        try {
            return path.evaluate(expression, item, returnType);
        } catch (XPathExpressionException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 获取 String
     *
     * @param expression 路径
     *
     * @return String
     */
    public String getString(String expression) {
        return (String) evalXPath(expression, null, XPathConstants.STRING);
    }

    /**
     * 获取 Boolean
     *
     * @param expression 路径
     *
     * @return String
     */
    public Boolean getBoolean(String expression) {
        return (Boolean) evalXPath(expression, null, XPathConstants.BOOLEAN);
    }

    /**
     * 获取 Number
     *
     * @param expression 路径
     *
     * @return {Number}
     */
    public Number getNumber(String expression) {
        return (Number) evalXPath(expression, null, XPathConstants.NUMBER);
    }

    /**
     * 获取某个节点
     *
     * @param expression 路径
     *
     * @return {Node}
     */
    public Node getNode(String expression) {
        return (Node) evalXPath(expression, null, XPathConstants.NODE);
    }

    /**
     * 获取子节点
     *
     * @param expression 路径
     *
     * @return NodeList
     */
    public NodeList getNodeList(String expression) {
        return (NodeList) evalXPath(expression, null, XPathConstants.NODESET);
    }

    /**
     * 获取 String
     *
     * @param node       节点
     * @param expression 相对于 node 的路径
     *
     * @return String
     */
    public String getString(Object node, String expression) {
        return (String) evalXPath(expression, node, XPathConstants.STRING);
    }

    /**
     * 获取
     *
     * @param node       节点
     * @param expression 相对于 node 的路径
     *
     * @return String
     */
    public Boolean getBoolean(Object node, String expression) {
        return (Boolean) evalXPath(expression, node, XPathConstants.BOOLEAN);
    }

    /**
     * 获取
     *
     * @param node       节点
     * @param expression 相对于 node 的路径
     *
     * @return {Number}
     */
    public Number getNumber(Object node, String expression) {
        return (Number) evalXPath(expression, node, XPathConstants.NUMBER);
    }

    /**
     * 获取某个节点
     *
     * @param node       节点
     * @param expression 路径
     *
     * @return {Node}
     */
    public Node getNode(Object node, String expression) {
        return (Node) evalXPath(expression, node, XPathConstants.NODE);
    }

    /**
     * 获取子节点
     *
     * @param node       节点
     * @param expression 相对于 node 的路径
     *
     * @return NodeList
     */
    public NodeList getNodeList(Object node, String expression) {
        return (NodeList) evalXPath(expression, node, XPathConstants.NODESET);
    }

    /**
     * 内部类单例
     */
    @Slf4j
    private static class XmlHelperHolder {

        private static final String FEATURE_HTTP_XML_ORG_SAX_FEATURES_EXTERNAL_GENERAL_ENTITIES = "https://xml.org/sax/features/external-general-entities";

        private static final String FEATURE_HTTP_XML_ORG_SAX_FEATURES_EXTERNAL_PARAMETER_ENTITIES = "https://xml.org/sax/features/external-parameter-entities";

        private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = XmlHelperHolder
                .newDocumentBuilderFactory();

        private static final DocumentBuilderFactory UNSAFE_DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory
                .newInstance();

        private static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();

        private static DocumentBuilderFactory newDocumentBuilderFactory() {
            DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
            // Set parser features to prevent against XXE etc.
            // Note: setting only the external entity features on DocumentBuilderFactory
            // instance
            // (ala how safeTransform does it for SAXTransformerFactory) does seem to work
            // (was still
            // processing the entities - tried Oracle JDK 7 and 8 on OSX). Setting seems a
            // bit extreme,
            // but looks like there's no other choice.
            df.setXIncludeAware(false);
            df.setExpandEntityReferences(false);
            df.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            df.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            setDocumentBuilderFactoryFeature(df, XMLConstants.FEATURE_SECURE_PROCESSING, true);
            setDocumentBuilderFactoryFeature(df, FEATURE_HTTP_XML_ORG_SAX_FEATURES_EXTERNAL_GENERAL_ENTITIES, false);
            setDocumentBuilderFactoryFeature(df, FEATURE_HTTP_XML_ORG_SAX_FEATURES_EXTERNAL_PARAMETER_ENTITIES, false);
            setDocumentBuilderFactoryFeature(df, "https://apache.org/xml/features/disallow-doctype-decl", true);
            setDocumentBuilderFactoryFeature(df, "https://apache.org/xml/features/nonvalidating/load-external-dtd",
                    false);
            return df;
        }

        private static void setDocumentBuilderFactoryFeature(DocumentBuilderFactory documentBuilderFactory,
                                                             String feature, boolean state) {
            try {
                documentBuilderFactory.setFeature(feature, state);
            } catch (Exception e) {
                log.warn("Failed to set the XML Document Builder factory feature {} to {}", feature, state, e);
            }
        }

    }

}