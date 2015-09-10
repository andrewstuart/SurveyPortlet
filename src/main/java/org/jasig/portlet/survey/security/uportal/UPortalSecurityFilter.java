/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portlet.survey.security.uportal;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.RenderFilter;
import javax.portlet.filter.FilterConfig;
import org.jasig.portlet.survey.security.UPortalUserDetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * This Security Filter, for use with Spring Security, implements
 * {@link RenderFilter} When SSP is used with uportal, this class populates the
 * session with the user's username and authorities for consumption by the http
 * servlet api.
 */
public final class UPortalSecurityFilter implements RenderFilter {

    public static final String ROLE_SURVEY_ADMIN = "ROLE_SURVEY_ADMIN";
    public static final String ROLE_SURVEY_USER = "ROLE_SURVEY_USER";

    public static final String AUTHENTICATION_TOKEN_KEY
            = UPortalSecurityFilter.class.getName() + ".GRANTED_AUTHORITIES_KEY";
    public static final Object SSP_OWNER = "SSP";

    private static final Logger logger = LoggerFactory
            .getLogger(UPortalSecurityFilter.class);

    @Override
    public void doFilter(final RenderRequest req, final RenderResponse res,
            final FilterChain chain) throws IOException, PortletException {

        final String principal = req.getRemoteUser();

        if (principal != null) { // User is authenticated

            final PortletSession session = req.getPortletSession();

            @SuppressWarnings("unchecked")
            final Set<GrantedAuthority> authorities = (Set<GrantedAuthority>) session.getAttribute(AUTHENTICATION_TOKEN_KEY);

            if (authorities == null) {
                populateAuthorites(req, principal);
            }
        }

        chain.doFilter(req, res);
    }

    private void populateAuthorites(final RenderRequest req, final String principal) {

        // But the user's access has not yet been established...
        final Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();

        boolean isSurveyAdmin = req.isUserInRole("survey-admin");
        if (isSurveyAdmin) {
            authorities.add(new SimpleGrantedAuthority(ROLE_SURVEY_ADMIN));
        }

        boolean isSurveyUser = req.isUserInRole("survey-user");
        if (isSurveyUser) {
            authorities.add(new SimpleGrantedAuthority(ROLE_SURVEY_USER));
        }

        logger.debug("Setting up GrantedAutorities for user '{}' -- {}",
                principal, authorities.toString());

        final UPortalUserDetails userDetails = new UPortalUserDetails(principal, Collections.unmodifiableSet(authorities));

        // Add UserDetails to Session
        final PortletSession session = req.getPortletSession();
        session.setAttribute(AUTHENTICATION_TOKEN_KEY, userDetails,
                PortletSession.APPLICATION_SCOPE);
    }

    @Override
    public void init(final FilterConfig config) throws PortletException {
        // nothing to do
    }

    @Override
    public void destroy() {
        // nothing to do
    }
}
