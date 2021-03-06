package web.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import web.entities.Article;

@Service
public class ArticleService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<Long, Article> idxMain = new WeakHashMap<>(500);

    @Value("${holarse.directories.articles}")
    private String directory;

    @PostConstruct
    public void initArticlesFromDisk() throws IOException {
        logger.debug("--------------------------------------------------------------------------------------------------");
        logger.debug("Loading articles from " + directory);
        idxMain.clear();
        Files.list(Paths.get(directory)).filter(Files::isRegularFile).forEach(p -> loadArticleFromDisk(p.toFile()));
        logger.debug("--------------------------------------------------------------------------------------------------");
        logger.debug("Loaded: {} articles", idxMain.size());
        logger.debug("--------------------------------------------------------------------------------------------------");
    }

    public void writeArticleToDisk(final Article article) {
        final File targetFile = new File(directory, article.getUid() + ".xml");

        logger.debug("Trying to write article {} ({}).", article.getUid(), targetFile.getAbsolutePath());
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(Article.class);
            final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.marshal(article, targetFile);

        } catch (JAXBException e) {
            logger.error("Fehler beim Schreiben der XML-Datei {}", targetFile.getAbsolutePath(), e);
        }

    }

    protected void loadArticleFromDisk(final File file) {
        logger.debug("Trying to load file " + file.getAbsolutePath());
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(Article.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            final Article article = (Article) jaxbUnmarshaller.unmarshal(file);

            idxMain.put(article.getUid(), article);

            logger.debug("{}", article);
            
        } catch (JAXBException e) {
            logger.error("Fehler beim Laden der XML-Datei {}", file.getAbsoluteFile(), e);
        }
    }

    public Article findById(final Long uid) {
        return idxMain.get(uid);
    }
    
    public Collection<Article> findAll() {
        return idxMain.values();
    }

}
