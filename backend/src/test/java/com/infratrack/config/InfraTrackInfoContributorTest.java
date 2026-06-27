package com.infratrack.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Instant;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InfraTrackInfoContributorTest {

    @Test
    void contribute_shouldExposeApplicationAndBuildMetadata() {
        BuildProperties buildProperties = new BuildProperties(new Properties() {{
            setProperty("version", "1.0.1");
            setProperty("time", Instant.parse("2026-06-27T08:00:00Z").toString());
        }});
        GitProperties gitProperties = new GitProperties(new Properties() {{
            setProperty("commit.id.abbrev", "abc1234");
        }});

        @SuppressWarnings("unchecked")
        ObjectProvider<BuildProperties> buildProvider = mock(ObjectProvider.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<GitProperties> gitProvider = mock(ObjectProvider.class);
        when(buildProvider.getIfAvailable()).thenReturn(buildProperties);
        when(gitProvider.getIfAvailable()).thenReturn(gitProperties);

        InfraTrackInfoContributor contributor = new InfraTrackInfoContributor(buildProvider, gitProvider);
        Info.Builder builder = new Info.Builder();
        contributor.contribute(builder);
        Info info = builder.build();

        @SuppressWarnings("unchecked")
        var application = (java.util.Map<String, Object>) info.getDetails().get("application");
        @SuppressWarnings("unchecked")
        var build = (java.util.Map<String, Object>) info.getDetails().get("build");

        assertThat(application.get("name")).isEqualTo("InfraTrack");
        assertThat(application.get("version")).isEqualTo("1.0.1");
        assertThat(build.get("commit")).isEqualTo("abc1234");
        assertThat(build.get("time")).isNotNull();
    }
}
