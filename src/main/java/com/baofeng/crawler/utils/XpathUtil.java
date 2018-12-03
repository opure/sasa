package com.baofeng.crawler.utils;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Created by opure on 2018/12/3.
 */
public class XpathUtil {

    private static Logger logger = LoggerFactory.getLogger(XpathUtil.class);
    static HtmlCleaner hc = new HtmlCleaner();

    public static NodeList getNodeList(String html, String exp) {
        TagNode tn = hc.clean(html);
        NodeList nodeList = null;
        org.w3c.dom.Document dom;
        try {
            dom = new DomSerializer(new CleanerProperties()).createDOM(tn);
            XPath xPath = XPathFactory.newInstance().newXPath();
            nodeList = (NodeList) xPath.compile(exp).evaluate(dom, XPathConstants.NODESET);
        } catch (ParserConfigurationException e) {
            logger.error("exp is {}, ParserConfigurationException is", exp, e);
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            logger.error("exp is {}, XPathExpressionException is ", exp, e);
        }
        return nodeList;
    }
}
