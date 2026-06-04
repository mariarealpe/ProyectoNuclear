package co.edu.cue.practicas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita @Scheduled para los jobs de recordatorios y reintentos
 * (RF-08-05, RF-08-06, RF-11-05).
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
}
