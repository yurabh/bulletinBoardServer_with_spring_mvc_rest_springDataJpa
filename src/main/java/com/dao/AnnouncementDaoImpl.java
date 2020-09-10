package com.dao;

import com.domain.Announcement;
import com.domain.Announcement_;
import com.domain.Heading;
import com.domain.Heading_;
import com.repository.AnnouncementRepository;
import com.service.EmailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.List;

/**
 * {@link AnnouncementDaoImpl} class serves for the data access process for {@link Announcement} in database
 * has extended CRUD methods and other methods for getting need additional data from database
 *
 * @author Yuriy Bahlay
 * @version 1.1
 */

@Transactional
@Repository
public class AnnouncementDaoImpl implements AnnouncementDao {

    /**
     * This is object instance of {@link EntityManager} helps us persist data into data base
     */
    @PersistenceContext
    private EntityManager entityManager;


    /**
     * Field {@link AnnouncementRepository} is object instance of {@link AnnouncementRepository} interface,
     * it helps us to perform database manipulations
     */
    private AnnouncementRepository announcementRepository;


    /**
     * This field is object instance of {@link EmailServiceImpl} helps us to work with sending e-mail
     */
    private EmailServiceImpl emailServiceImpl;


    /**
     * This is a constructor for creates object {@link AnnouncementDaoImpl} by default without parameters
     */
    public AnnouncementDaoImpl() {
    }


    /**
     * This is a constructor {@link AnnouncementDaoImpl} with parameters that injects two objects gain of the
     * {@link AnnouncementRepository} and {@link EmailServiceImpl} classes
     *
     * @param announcementRepository {@link AnnouncementRepository}
     * @param emailServiceImpl       {@link EmailServiceImpl}
     */
    @Autowired
    public AnnouncementDaoImpl(AnnouncementRepository announcementRepository,
                               EmailServiceImpl emailServiceImpl) {
        this.announcementRepository = announcementRepository;
        this.emailServiceImpl = emailServiceImpl;
    }


    /**
     * This class method stores the {@link Announcement} in a database and conducts and collaborates with
     * {@link EmailServiceImpl} to find emails to send mailings
     *
     * @param announcement {@link Announcement}
     */
    @Override
    public void save(Announcement announcement) {
        entityManager.persist(announcement);
        emailServiceImpl.searchEmailsForSendingEmail(announcement);
    }


    /**
     * This class method searches the {@link Announcement} for the announcement id in a database
     *
     * @param id int announcement id
     * @return {@link Announcement} found or null
     */
    @Override
    @Transactional(readOnly = true)
    public Announcement find(int id) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Announcement> criteriaQuery = criteriaBuilder.createQuery(Announcement.class);

        Root<Announcement> from = criteriaQuery.from(Announcement.class);

        Path<Integer> annId = from.get(Announcement_.id);

        criteriaQuery.select(from);

        criteriaQuery.where(criteriaBuilder.equal(annId, id));

        TypedQuery<Announcement> query = entityManager.createQuery(criteriaQuery);

        return query.getResultStream().findFirst().orElse(null);
    }


    /**
     * This class method updates the {@link Announcement} in the database and accepts the {@link Announcement}
     * object with the new data for updating
     *
     * @param announcement {@link Announcement}
     */
    @Override
    public void update(Announcement announcement) {
        Announcement merge = entityManager.merge(announcement);
        entityManager.persist(merge);
    }


    /**
     * This class method removes the {@link Announcement} from the database and accepts the ID to delete
     *
     * @param id int
     */
    @Override
    public void delete(int id) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaDelete<Announcement> deleteAnnouncement = criteriaBuilder.createCriteriaDelete(Announcement.class);

        Root<Announcement> from = deleteAnnouncement.from(Announcement.class);

        deleteAnnouncement.where(criteriaBuilder.equal(from.get("id"), id));

        entityManager.createQuery(deleteAnnouncement).executeUpdate();
    }


    /**
     * This class method removes the {@link Announcement} from the database and accepts the ID to delete
     *
     * @param id int
     */
    @Override
    public void deleteAnnouncementById(int id) {
        Query query = entityManager.createQuery("DELETE FROM Announcement a WHERE a.id = :id");

        query.setParameter("id", id);

        query.executeUpdate();
    }


    /**
     * This class method removes all announcements from data base by {@link Heading} ID
     *
     * @param id int
     */
    @Override
    public void deleteByHeading(int id) {
        Query query = entityManager.createQuery("DELETE FROM Announcement a WHERE a.heading.id = :id");

        query.setParameter("id", id);

        query.executeUpdate();
    }


    /**
     * This class method removes all announcements from data base by heading ID
     *
     * @param id int
     */
    @Override
    public void deleteAllFromHeading(int id) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaDelete<Announcement> criteriaDelete = criteriaBuilder.createCriteriaDelete(Announcement.class);

        Root<Announcement> root = criteriaDelete.from(Announcement.class);

        Path<Integer> path = root.get(Announcement_.heading).get(Heading_.ID);

        criteriaDelete.where(criteriaBuilder.equal(path, id));

        entityManager.createQuery(criteriaDelete).executeUpdate();
    }


    /**
     * This class method gets adjusted amount of announcements from data base on a pages
     *
     * @param page int
     * @param size int
     * @return {@link List<Announcement>}
     */
    @Override
    public List<Announcement> getSomePagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Announcement> pages = announcementRepository.findAll(pageable);
        return pages.getContent();
    }


    /**
     * This class method delete announcements from database when they marked as inactive
     */
    @Override
    public void deleteNoActiveAnnouncements() {
        Query query = entityManager.createQuery("DELETE FROM Announcement a WHERE a.active = false");

        query.executeUpdate();
    }
}
