package konkuk.thip.book.adapter.out.api;

import konkuk.thip.book.adapter.out.api.dto.NaverBookParseResult;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.code.ErrorCode;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.InputSource;

public class NaverBookXmlParser {

    public static NaverBookParseResult parse(String xml) {
        List<NaverBookParseResult.NaverBook> Naverbooks = new ArrayList<>();
        int total = -1;
        int start = -1;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            Document doc = builder.parse(is);

            NodeList channelNodes = doc.getElementsByTagName("channel");
            if (channelNodes.getLength() > 0) {
                Element channel = (Element) channelNodes.item(0);
                total = Integer.parseInt(getTagValue(channel, "total"));
                start = Integer.parseInt(getTagValue(channel, "start"));

                NodeList itemNodes = channel.getElementsByTagName("item");
                for (int i = 0; i < itemNodes.getLength(); i++) {
                    Element item = (Element) itemNodes.item(i);
                    String title = getTagValue(item, "title");
                    String imageUrl = getTagValue(item, "image");
                    String author = getTagValue(item, "author");
                    String publisher = getTagValue(item, "publisher");
                    String isbn = getTagValue(item, "isbn");
                    NaverBookParseResult.NaverBook naverBook = NaverBookParseResult.NaverBook.builder()
                            .title(title)
                            .imageUrl(imageUrl)
                            .author(author)
                            .publisher(publisher)
                            .isbn(isbn)
                            .build();
                    Naverbooks.add(naverBook);
                }
            }
        } catch (Exception e) {
           throw new BusinessException(ErrorCode.BOOK_NAVER_API_PARSING_ERROR);
        }
        return NaverBookParseResult.of(Naverbooks, total, start);
    }

    private static String getTagValue(Element element, String tag) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList.getLength() > 0 && nodeList.item(0).getFirstChild() != null) {
            return nodeList.item(0).getFirstChild().getNodeValue();
        }
        return "";
    }

}
