package engine.os.docker_compose

import engine.os.DockerComposeYaml

object GraphiteStatsDGrafanaYaml extends DockerComposeYaml {
  lazy val get: String =
    """version: '3'
      |services:
      |  graphite:
      |    image: graphiteapp/graphite-statsd
      |    container_name: graphite
      |    ports:
      |      - "8080:80"        # Graphite web UI
      |      - "2003:2003"      # Graphite plaintext TCP receiver
      |      - "8125:8125/udp"  # StatsD UDP receiver
      |    environment:
      |      - GRAPHITE_MAX_SIZE=100000000
      |      - GRAPHITE_STORAGE_DIR=/opt/graphite/storage
      |
      |  grafana:
      |    image: grafana/grafana
      |    container_name: grafana
      |    ports:
      |      - "3000:3000"      # Grafana UI
      |    depends_on:
      |      - graphite
      |    environment:
      |      - GF_SECURITY_ADMIN_PASSWORD=admin
      |    volumes:
      |      - grafana-storage:/var/lib/grafana
      |
      |volumes:
      |  grafana-storage:
      |""".stripMargin
}
