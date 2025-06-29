package konkuk.thip.book.adapter.out.api;

import konkuk.thip.book.adapter.out.api.dto.NaverBookParseResult;
import konkuk.thip.book.adapter.out.api.dto.NaverDetailBookParseResult;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.code.ErrorCode;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.InputSource;

import static konkuk.thip.common.exception.code.ErrorCode.BOOK_ISBN_NOT_FOUND;

public class NaverBookXmlParser {

    public static NaverBookParseResult parseBookList(String xml) {
        List<NaverBookParseResult.NaverBook> naverBooks = new ArrayList<>();
        int total = -1;
        int start = -1;
        try {
            Element channel = getFirstChannel(xml);
            if (channel != null) {
                total = Integer.parseInt(getTagValue(channel, "total"));
                start = Integer.parseInt(getTagValue(channel, "start"));

                for (Element item : getItemElements(channel)) {
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
                    naverBooks.add(naverBook);
                }
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.BOOK_NAVER_API_PARSING_ERROR);
        }
        return NaverBookParseResult.of(naverBooks, total, start);
    }

    public static NaverDetailBookParseResult parseBookDetail(String xml) {
        try {
            Element channel = getFirstChannel(xml);
            if (channel != null) {
                int total = 0;
                String totalStr = getTagValue(channel, "total");
                if (totalStr != null) total = Integer.parseInt(totalStr);

                // total이 0이면 isbn에 해당하는 책이 없음(잘못 넘어온 isbn 예외처리)
                if (total == 0) throw new BusinessException(BOOK_ISBN_NOT_FOUND);

                List<Element> items = getItemElements(channel);
                if (!items.isEmpty()) {
                    Element item = items.get(0);
                    String title = getTagValue(item, "title");
                    String imageUrl = getTagValue(item, "image");
                    String author = getTagValue(item, "author");
                    String publisher = getTagValue(item, "publisher");
                    String isbn = getTagValue(item, "isbn");
                    String description = getTagValue(item, "description");

                    return NaverDetailBookParseResult.builder()
                            .title(title)
                            .imageUrl(imageUrl)
                            .author(author)
                            .publisher(publisher)
                            .isbn(isbn)
                            .description(description)
                            .build();
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.BOOK_NAVER_API_PARSING_ERROR);
        }
        return null;
    }


    private static Element getFirstChannel(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        Document doc = builder.parse(is);

        NodeList channelNodes = doc.getElementsByTagName("channel");
        if (channelNodes.getLength() > 0) {
            return (Element) channelNodes.item(0);
        }
        return null;
    }

    private static List<Element> getItemElements(Element channel) {
        NodeList itemNodes = channel.getElementsByTagName("item");
        List<Element> items = new ArrayList<>();
        for (int i = 0; i < itemNodes.getLength(); i++) {
            items.add((Element) itemNodes.item(i));
        }
        return items;
    }

    private static String getTagValue(Element element, String tag) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList.getLength() > 0 && nodeList.item(0).getFirstChild() != null) {
            return nodeList.item(0).getFirstChild().getNodeValue();
        }
        return "";
    }

}
