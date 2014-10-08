<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="pic" type="com.aetrion.flickr.photos.Photo"--%>
<%--@elvariable id="flickrGalleryPhotos" type="org.jahia.modules.flickr.Photo"--%>

<jcr:nodeProperty node="${currentNode}" name="title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="photoURL" var="photoURL"/>

<c:forEach items="${jcr:getChildrenOfType(currentNode, 'jnt:file')}" var="subnode">
    <c:if test="${subnode.name == 'binary'}">
        <c:set value="${subnode}" var="binary"/>
    </c:if>
</c:forEach>

<h2>${title.string}</h2>
<c:set value="${photoURL.string}" var="url"/>

<c:if test="${not empty binary}">
    <c:set value="${binary.url}" var="url"/>
</c:if>

<img src="${url}" alt="${title.string}" width="100%"/>
