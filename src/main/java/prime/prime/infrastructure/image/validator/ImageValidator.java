package prime.prime.infrastructure.image.validator;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.image.HeifParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

public class ImageValidator implements ConstraintValidator<ValidImage, MultipartFile> {

    private static final int MAX_DIMENSION = 1200;

    private Tika tika = new Tika();

    @Override
    public void initialize(ValidImage constraintAnnotation) {

    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext context) {

        if (multipartFile != null) {
            try {
                String contentType = tika.detect(multipartFile.getInputStream());
                assert contentType != null;

                if (!isSupportedContentType(contentType)) {
                    return false;
                }

                if (!isWithinDimensions(multipartFile, contentType)) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Image dimensions must not exceed 1200x1200.").addConstraintViolation();
                    return false;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return true;
    }

    private boolean isSupportedContentType(String contentType) {
        return contentType.equals("image/png")
            || contentType.equals("image/jpg")
            || contentType.equals("image/jpeg")
            || isHeicFormat(contentType);
    }

    private boolean isHeicFormat(String contentType) {
        return contentType.equals("image/heic") || contentType.equals("image/heif");
    }

    private boolean isWithinDimensions(MultipartFile multipartFile, String contentType)
        throws IOException, TikaException, SAXException {

        if (isHeicFormat(contentType)) {
            Metadata image = getHeifImgMetadata(multipartFile);

            String height = image.get("Height").replaceAll("[^0-9]", "");
            String width = image.get("Width").replaceAll("[^0-9]", "");

            return Integer.parseInt(height) <= MAX_DIMENSION && Integer.parseInt(width) <= MAX_DIMENSION;
        }

        BufferedImage img = ImageIO.read(multipartFile.getInputStream());
        return img.getHeight() <= MAX_DIMENSION && img.getWidth() <= MAX_DIMENSION;
    }

    private Metadata getHeifImgMetadata(MultipartFile multipartFile)
        throws IOException, TikaException, SAXException {
        Metadata metadata = new Metadata();
        BodyContentHandler bodyContentHandler = new BodyContentHandler();
        HeifParser heifParser = new HeifParser();

        heifParser.parse(multipartFile.getInputStream(), bodyContentHandler, metadata, new ParseContext());
        return metadata;
    }
}
