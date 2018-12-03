package com.baofeng.crawler.service;

import com.baofeng.crawler.domain.FetchAsin;
import org.jsoup.nodes.Element;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.util.List;

/**
 * Created by opure on 2018/12/3.
 */
public interface ParseHtmlService {

    List<Element> parseHtmlDate(String html, String url, FetchAsin fetchAsin) throws ParserConfigurationException, XPathExpressionException;
}
