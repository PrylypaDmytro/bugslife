package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.model.App;
import com.example.repository.AppRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

@Service
@Transactional(readOnly = true)
public class AppService {

	@Autowired
	private AppRepository appRepository;

	public List<App> findAll() {
		return appRepository.findAll();
	}

	public Optional<App> findOne(Long id) {
		return appRepository.findById(id);
	}

	@Transactional(readOnly = false)
	public App save(App entity) {
		// Set the default value for the URL if it is empty or null
		if (entity.getUrl() == null || entity.getUrl().isEmpty()) {
			HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes())
					.getRequest();
			String currentURL = request.getRequestURL().toString();
			entity.setUrl(currentURL);
		}
		return appRepository.save(entity);
	}

	@Transactional(readOnly = false)
	public void delete(App entity) {
		appRepository.delete(entity);
	}

}
