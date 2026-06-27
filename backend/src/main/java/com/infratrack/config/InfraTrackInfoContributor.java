package com.infratrack.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class InfraTrackInfoContributor implements InfoContributor {

    private static final String APPLICATION_NAME = "InfraTrack";

    private final ObjectProvider<BuildProperties> buildProperties;
    private final ObjectProvider<GitProperties> gitProperties;

    public InfraTrackInfoContributor(
            ObjectProvider<BuildProperties> buildProperties,
            ObjectProvider<GitProperties> gitProperties) {
        this.buildProperties = buildProperties;
        this.gitProperties = gitProperties;
    }

    @Override
    public void contribute(Info.Builder builder) {
        BuildProperties build = buildProperties.getIfAvailable();
        GitProperties git = gitProperties.getIfAvailable();

        Map<String, Object> application = new LinkedHashMap<>();
        application.put("name", APPLICATION_NAME);
        application.put("version", build != null ? build.getVersion() : "unknown");
        builder.withDetail("application", application);

        Map<String, Object> buildInfo = new LinkedHashMap<>();
        if (build != null && build.getTime() != null) {
            buildInfo.put("time", build.getTime().toString());
        }
        buildInfo.put("commit", resolveCommitHash(git));
        builder.withDetail("build", buildInfo);
    }

    private String resolveCommitHash(GitProperties git) {
        if (git == null) {
            return "unknown";
        }
        String commit = git.getShortCommitId();
        return commit != null && !commit.isBlank() ? commit : "unknown";
    }
}
