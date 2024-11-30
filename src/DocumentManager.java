import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final List<Document> documents = new ArrayList<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {

        if (document.getId() == null) {
            document.setId(UUID.randomUUID().toString());
            documents.add(document);
        } else {
            // update the document if already exists
            Optional<Document> existingDocument = findById(document.getId());
            existingDocument.ifPresent(documents::remove);
            documents.add(document);
        }

        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        if (request == null) {
            return Collections.emptyList();
        }

        return documents.stream()
                .filter(doc -> findMatches(doc, request))
                .collect(Collectors.toList());

    }

    public boolean findMatches(Document document, SearchRequest request) {
        if (request.getTitlePrefixes() != null && !request.getTitlePrefixes().isEmpty()) {
            boolean titleMatches = request.titlePrefixes.stream()
                    .anyMatch(prefix -> document.getTitle() != null && document.getTitle().startsWith(prefix));

            if (!titleMatches) return false;
        }

        if (request.getContainsContents() != null && !request.getContainsContents().isEmpty()) {
            boolean contentMatches = request.containsContents.stream()
                    .anyMatch(content -> document.getContent() != null && document.getContent().startsWith(content));

            if (!contentMatches) return false;
        }

        if (request.getAuthorIds() != null && !request.getAuthorIds().isEmpty()) {
            boolean authorIdMatches = request.authorIds.stream()
                    .anyMatch(id -> document.getId() != null && document.getId().startsWith(id));

            if (!authorIdMatches) return false;
        }

        if (request.getCreatedFrom() != null &&
                document.getCreated() != null &&
                document.getCreated().isBefore(request.getCreatedFrom())) {

            return false;
        }

        if (request.getCreatedTo() != null &&
                document.getCreated() != null &&
                document.getCreated().isAfter(request.getCreatedTo())) {

            return false;
        }

        return true;
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {

        return documents.stream()
                .filter(doc -> doc.getId().equals(id))
                .findFirst();
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}