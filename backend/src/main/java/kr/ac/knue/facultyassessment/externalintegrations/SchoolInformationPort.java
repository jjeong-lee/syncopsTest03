package kr.ac.knue.facultyassessment.externalintegrations;

import java.util.List;

public interface SchoolInformationPort {

    List<SchoolInformation> lookup(SchoolInformationQuery query);

    record SchoolInformationQuery(
        String schoolName,
        String educationOfficeCode,
        int pIndex,
        int pSize
    ) {
    }

    record SchoolInformation(
        String educationOfficeName,
        String schoolName,
        String schoolTypeName,
        String locationName,
        String foundationName,
        String roadAddress,
        String telephoneNumber
    ) {
    }

    class ExternalLookupException extends RuntimeException {

        public ExternalLookupException(String message) {
            super(message);
        }

        public ExternalLookupException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
