package com.photopia.photopia_back.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Date;

@Service
public class ImageMetadataService {

    private static final Logger logger = LoggerFactory.getLogger(ImageMetadataService.class);

    public record ImageMetadata(
            Integer width,
            Integer height,
            Date takenAt,
            Double latitude,
            Double longitude) {
    }

    public ImageMetadata extractMetadata(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
            return processMetadata(metadata);
        } catch (Exception e) {
            logger.error("Failed to extract metadata for file: {}", file.getOriginalFilename(), e);
            return new ImageMetadata(null, null, null, null, null);
        }
    }

    private ImageMetadata processMetadata(Metadata metadata) {
        Integer width = null;
        Integer height = null;
        Date takenAt = null;
        Double latitude = null;
        Double longitude = null;

        for (Directory directory : metadata.getDirectories()) {
            if (width == null)
                width = extractIntTag(directory, JpegDirectory.TAG_IMAGE_WIDTH, ExifDirectoryBase.TAG_EXIF_IMAGE_WIDTH);
            if (height == null)
                height = extractIntTag(directory, JpegDirectory.TAG_IMAGE_HEIGHT,
                        ExifDirectoryBase.TAG_EXIF_IMAGE_HEIGHT);

            if (takenAt == null && directory.containsTag(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)) {
                takenAt = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            }

            if (latitude == null && directory instanceof GpsDirectory gpsDirectory) {
                GeoLocation geoLocation = gpsDirectory.getGeoLocation();
                if (geoLocation != null) {
                    latitude = geoLocation.getLatitude();
                    longitude = geoLocation.getLongitude();
                }
            }
        }

        logger.info("Extracted Metadata -> W: {}, H: {}, Date: {}, GPS: {},{}", width, height, takenAt, latitude,
                longitude);
        return new ImageMetadata(width, height, takenAt, latitude, longitude);
    }

    private Integer extractIntTag(Directory directory, int... tags) {
        for (int tag : tags) {
            if (directory.containsTag(tag)) {
                return directory.getInteger(tag);
            }
        }
        return null;
    }
}
