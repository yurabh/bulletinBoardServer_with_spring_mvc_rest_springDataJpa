package com.service.impl;

import com.dao.AuthorDao;
import com.domain.Author;

import com.dto.AuthorDto;

import com.repository.AuthorRepository;
import com.security.jwt.JwtProvider;
import com.service.AuthorService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link AuthorServiceImpl} class binds realization part with user and
 * binds {@link AuthorDao} layer.
 *
 * @author Yuriy Bahlay.
 * @version 1.1.
 */

@Service
public class AuthorServiceImpl implements AuthorService {


    /**
     * Field {@link AuthorDao} is object instance of {@link AuthorDao}
     * interface, it helps us to perform database manipulations.
     */
    private final AuthorDao authorDao;


    /**
     * This is field {@link ModelMapper} for converting objects.
     */
    private final ModelMapper modelMapper;


    /**
     * This is field {@link BCryptPasswordEncoder} for encoding
     * password.
     */
    private final BCryptPasswordEncoder passwordEncoder;


    /**
     * This is field {@link AuthorRepository} for access
     * to author repository.
     */
    private final AuthorRepository authorRepository;


    /**
     * This is field {@link JwtProvider} for working with Jwt.
     */
    private final JwtProvider jwtProvider;


    /**
     * This is a constructor with parameters {@link AuthorServiceImpl}
     * that injects object gain of the {@link AuthorDao}
     * and others.
     *
     * @param daoAuthor        {@link AuthorDao}.
     * @param mapperModel      {@link ModelMapper}.
     * @param encoder          {@link BCryptPasswordEncoder}.
     * @param repositoryAuthor {@link AuthorRepository}.
     * @param providerJwt      {@link JwtProvider}.
     */
    @Autowired
    public AuthorServiceImpl(final AuthorDao daoAuthor,
                             final ModelMapper mapperModel,
                             final BCryptPasswordEncoder encoder,
                             final AuthorRepository repositoryAuthor,
                             final JwtProvider providerJwt) {
        this.authorDao = daoAuthor;
        this.modelMapper = mapperModel;
        this.passwordEncoder = encoder;
        this.authorRepository = repositoryAuthor;
        this.jwtProvider = providerJwt;
    }


    /**
     * This method takes the {@link AuthorDto} and transmits it
     * to the {@link AuthorDao#save(Object)} to save the author.
     *
     * @param authorDto {@link AuthorDto}.
     */
    @Override
    public void save(final AuthorDto authorDto) {
        final Author mappedToAuthor = modelMapper.map(authorDto, Author.class);
        mappedToAuthor.setPassword(passwordEncoder.
                encode(authorDto.getPassword()));
        authorDao.save(mappedToAuthor);
    }


    /**
     * This is method which is take {@link AuthorDto} object and
     * generate token and return it to the controller layer.
     *
     * @param authorDto {@link AuthorDto}.
     * @return string {@link String}.
     */
    @Override
    public String authentication(final AuthorDto authorDto) {
        final Author author = modelMapper.map(authorDto, Author.class);
        return jwtProvider.generateToken(author.getName());
    }


    /**
     * This is method for searching for an author by id and pass
     * {@link Author#id} to the {@link AuthorDao
     * #find(int)} to find the author in database.
     *
     * @param id int.
     * @return authorDto {@link AuthorDto}.
     */
    @Override
    public AuthorDto find(final int id) {
        final Author author = authorDao.find(id);
        if (author == null) {
            return null;
        }
        return modelMapper.map(author, AuthorDto.class);
    }


    /**
     * This is method accepts the {@link AuthorDto}
     * object with the new data and pass it to the {@link AuthorDao
     * #update(Object)} to update the author.
     *
     * @param authorDto {@link AuthorDto}.
     */
    @Override
    public void update(final AuthorDto authorDto) {
        final Author authorMapped = modelMapper.map(authorDto, Author.class);
        authorDao.update(authorMapped);
    }


    /**
     * This is method pass author id to the {@link AuthorDao
     * #delete(int)} to delete the author.
     *
     * @param id int.
     */
    @Override
    @Transactional
    public void delete(final int id) {
        authorRepository.deleteFromUserRole(id);
        authorDao.delete(id);
    }


    /**
     * This is method pass author id to the {@link AuthorDao
     * #deleteAnnouncementsByAuthorId(int)} to remove all announcements
     * by a specific author id.
     *
     * @param id int.
     */
    @Override
    public void deleteAnnouncementsByAuthorId(final int id) {
        authorDao.deleteAnnouncementsByAuthorId(id);
    }
}
