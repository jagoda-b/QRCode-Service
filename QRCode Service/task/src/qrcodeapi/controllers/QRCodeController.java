package qrcodeapi.controllers;

import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import qrcodeapi.generator.QRGenerator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

@RestController
public class QRCodeController {
    private static final String ERROR_CONTENTS = "Contents cannot be null or blank";
    private static final String ERROR_SIZE = "Image size must be between 150 and 350 pixels";
    private static final String ERROR_CORRECTION = "Permitted error correction levels are L, M, Q, H";
    private static final String ERROR_TYPE = "Only png, jpeg and gif image types are supported";

    private final QRGenerator qrGenerator;

    public QRCodeController(QRGenerator qrGenerator) {
        this.qrGenerator = qrGenerator;
    }

    @GetMapping(path = "/api/qrcode")
    public ResponseEntity<?> generateQRCode(@RequestParam(required = false, defaultValue = "") String contents,
                                            @RequestParam(required = false, defaultValue = "250") int size,
                                            @RequestParam(required = false, defaultValue = "L") String correction,
                                            @RequestParam(required = false, defaultValue = "png") String type) {
        Optional<String> error = validateParameters(contents, size, correction, type);
        if (error.isPresent()) {
            return ResponseEntity.badRequest().body(error.get());
        }
        ErrorCorrectionLevel errorCorrectionLevel = ErrorCorrectionLevel.valueOf(correction);

        try (var baos = new ByteArrayOutputStream()) {
            BufferedImage qrCodeImage = qrGenerator.generateQRCodeImage(contents, size, size, errorCorrectionLevel);
            ImageIO.write(qrCodeImage, type, baos);
            byte[] bytes = baos.toByteArray();
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/" + type)).body(bytes);
        } catch (IOException | WriterException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Optional<String> validateParameters(String contents, int size, String correction, String type) {
        if (contents == null || contents.trim().isEmpty()) {
            return Optional.of(ERROR_CONTENTS);
        }
        if (size < 150 || size > 350) {
            return Optional.of(ERROR_SIZE);
        }
        try {
            ErrorCorrectionLevel.valueOf(correction);
        } catch (IllegalArgumentException e) {
            return Optional.of(ERROR_CORRECTION);
        }
        if (!type.equals("png") && !type.equals("jpeg") && !type.equals("gif")) {
            return Optional.of(ERROR_TYPE);
        }
        return Optional.empty();
    }
}