package konkuk.thip.book.application.port.in.dto;


public record BookIsSavedResult(
        String isbn,
        boolean isSaved
)
{
    public static BookIsSavedResult of( String isbn,boolean isSaved) {
        return new BookIsSavedResult(isbn, isSaved);
    }
}
