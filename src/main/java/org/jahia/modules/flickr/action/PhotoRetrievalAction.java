package org.jahia.modules.flickr.action;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: tarek
 * Date: 5/11/13
 * Time: 15:55
 *
 * This action is called when we load a picture for detailed view in a content template.
 * When loading the picture the first time, we have to create a node in the repository to store additional info
 * for the picture (comments, rating...). If the user has checked the copyPhoto property, the action also copies the
 * binary as a subnode.
 */
public class PhotoRetrievalAction extends Action {

    static final Logger logger = LoggerFactory.getLogger(PhotoRetrievalAction.class);

    public ActionResult doExecute(HttpServletRequest httpServletRequest, RenderContext renderContext,
                                  Resource resource, JCRSessionWrapper jcrSessionWrapper, Map<String, List<String>> stringListMap, URLResolver urlResolver) throws Exception {

        final JCRNodeWrapper node = resource.getNode();

        // Get the photo ID from params
        final List<String> photoIDParam = stringListMap.get("photoID");
        String photoID = null;
        if (photoIDParam == null || (photoID = photoIDParam.get(0)) == null || photoID.trim().length() == 0) {
            logger.error("Impossible to create a pic without a pic ID");
            return ActionResult.BAD_REQUEST;
        }

        // Check if photo node already exists in JCR
        try {
            JCRNodeWrapper target = node.getNode(photoID);
        } catch (javax.jcr.PathNotFoundException e) {

            // Get photo data
            logger.info("Photo doesn't exist - retrieving");

            final List<String> photoTitleParam = stringListMap.get("photoTitle");
            String photoTitle = null;
            if (photoTitleParam == null || (photoTitle = photoTitleParam.get(0)) == null || photoTitle.trim().length() == 0) {
                logger.error("Impossible to create a pic without a title");
                return ActionResult.BAD_REQUEST;
            }

            final List<String> photoURLParam = stringListMap.get("photoURL");
            String photoURL = null;
            if (photoURLParam == null || (photoURL = photoURLParam.get(0)) == null || photoURL.trim().length() == 0) {
                logger.error("Impossible to create a pic without a URL");
                return ActionResult.BAD_REQUEST;
            }

            //Create the photo node object in the JCR
            final JCRNodeWrapper photoNode = node.addNode(photoID,"jnt:flickrPhoto");
            photoNode.setProperty("jcr:title", photoTitle);
            photoNode.setProperty("photoURL", photoURL);

            // Save the photo binary to Jahia if needed
            final List<String> copyPhotoParam = stringListMap.get("copyPhoto");
            if (copyPhotoParam != null && new Boolean(copyPhotoParam.get(0))) {
                try {
                    logger.info("Copying photo file (" + copyPhotoParam.get(0) + ")");

                    // Get picture binary file from Flickr
                    HttpClient httpClient = new HttpClient();
                    httpClient.getParams().setContentCharset("UTF-8");
                    GetMethod getMethod = new GetMethod(photoURL);

                    httpClient.executeMethod(getMethod);
                    InputStream instream =  getMethod.getResponseBodyAsStream();
                    BufferedInputStream bis = new BufferedInputStream(instream);

                    // Copy file to JCR
                    String mime = getMethod.getResponseHeader("Content-Type").getValue().split(";")[0].trim();
                    logger.info("Picture mime type: " + mime);
                    photoNode.uploadFile("binary", instream, mime);

                    instream.close();
                } catch (Exception exception) {
                    logger.error("Can't copy binary file to repository");
                    exception.printStackTrace();
                }

            }

            //Save the work
            jcrSessionWrapper.save();

            // This action can be requested from a non Ajax request, so we need to set up a redirection
            // If the action had been designed to be requested from Ajax requests only, we could have used a predefined ActionResult
            return new ActionResult(HttpServletResponse.SC_ACCEPTED,
                    renderContext.getMainResource().getNode().getPath());
        }

        logger.info("Photo exists - skipping");

        return ActionResult.OK;
    }
}
