package qrcodeapi.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@RestController
public class QRCodeController {

    @GetMapping(path = "/api/qrcode")
    public ResponseEntity<?> getQRCode(@RequestParam int size, @RequestParam String type) throws IOException {
        if (size < 150 || size > 350) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Image size must be between 150 and 350 pixels"));
        }

        if (!type.equals("png") && !type.equals("jpeg") && !type.equals("gif")) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Only png, jpeg and gif image types are supported"));
        }

        BufferedImage bufferedImage = generateWhiteImage(size, size);

        try (var baos = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, type, baos);
            byte[] bytes = baos.toByteArray();
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("image/" + type)).body(bytes);
        } catch (IOException e) {
            // handle the IOException
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public BufferedImage generateWhiteImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        return image;
    }


}