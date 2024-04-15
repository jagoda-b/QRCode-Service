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
import java.util.Map;

@RestController
public class QRCodeController {
    private final QRGenerator qrGenerator;

    public QRCodeController(QRGenerator qrGenerator) {
        this.qrGenerator = qrGenerator;
    }

    @GetMapping(path = "/api/qrcode")
    public ResponseEntity<?> generateQRCode(@RequestParam(required = false, defaultValue = "") String contents,
                                            @RequestParam(required = false, defaultValue = "250") int size,
                                            @RequestParam(required = false, defaultValue = "L") String correction,
                                            @RequestParam(required = false, defaultValue = "png") String type) {
        if (contents == null || contents.trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Contents cannot be null or blank"));
        }
        if (size < 150 || size > 350) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Image size must be between 150 and 350 pixels"));
        }
        ErrorCorrectionLevel errorCorrectionLevel;
        switch (correction) {
            case "L":
                errorCorrectionLevel = ErrorCorrectionLevel.L;
                break;
            case "M":
                errorCorrectionLevel = ErrorCorrectionLevel.M;
                break;
            case "Q":
                errorCorrectionLevel = ErrorCorrectionLevel.Q;
                break;
            case "H":
                errorCorrectionLevel = ErrorCorrectionLevel.H;
                break;
            default:
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "Permitted error correction levels are L, M, Q, H"));
        }
        if (!type.equals("png") && !type.equals("jpeg") && !type.equals("gif")) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Only png, jpeg and gif image types are supported"));
        }

        try (var baos = new ByteArrayOutputStream()) {
            BufferedImage qrCodeImage = qrGenerator.generateQRCodeImage(contents, size, size, errorCorrectionLevel);
            ImageIO.write(qrCodeImage, type, baos);
            byte[] bytes = baos.toByteArray();
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/" + type)).body(bytes);
        } catch (IOException e) {
            // handle the IOException
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (WriterException e) {
            // handle the WriterException
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}