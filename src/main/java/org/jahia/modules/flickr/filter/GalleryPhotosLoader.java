package org.jahia.modules.flickr.filter;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.people.PeopleInterface;
import com.aetrion.flickr.people.User;
import com.aetrion.flickr.photos.Photo;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

public class GalleryPhotosLoader extends AbstractFilter {
    static final Logger logger =  LoggerFactory.getLogger(GalleryPhotosLoader.class);

    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) {

        // Get developer credentials for Flickr from component properties
        String apiKey = resource.getNode().getPropertyAsString("apiKey");
        String sharedSecret = resource.getNode().getPropertyAsString("sharedSecret");


        try  {
            String username = resource.getNode().getPropertyAsString("userID");
            Long maxPhotos = resource.getNode().getProperty("maxPhotos").getLong();
            maxPhotos = (maxPhotos == null ? new Long(0) : maxPhotos );

            // Get Flickr API
            Flickr f = new Flickr(apiKey, sharedSecret, new REST());
            Flickr.debugRequest = false;
            Flickr.debugStream = false;

            // Get user ID
            PeopleInterface peopleIface = f.getPeopleInterface();
            User user = peopleIface.findByUsername(username);
            logger.debug("Found user: " + user.getId() + " / " + user.getRealName());

            // Get pictures
            List photosList = peopleIface.getPublicPhotos(user.getId(), maxPhotos.intValue(), 0);

            for (int i=0; i<photosList .size(); i++) {
                Photo photo = (Photo) photosList .get(i);
                photo.setUrl(photo.getLargeUrl());
                logger.debug("Found picture: " + photo.getTitle());
            }

            // Save Pictures List in request
            HttpServletRequest request = renderContext.getRequest();
            request.setAttribute("flickrGalleryPhotos", photosList);

        } catch (RepositoryException e) {
            logger.error("Missing information for Flickr gallery");
            e.printStackTrace();
        } catch (FlickrException e) {
            logger.error("Could not get data from Flickr");
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
