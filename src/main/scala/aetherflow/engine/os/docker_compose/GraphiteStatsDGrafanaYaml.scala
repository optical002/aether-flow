package aetherflow.engine.os.docker_compose

import aetherflow.engine.os.DockerComposeYaml

class GraphiteStatsDGrafanaYaml(
  graphiteUIPort: Int = 8080,
  grafanaPort: Int = 3000,
  graphiteTCP: Int = 2003,
  statsdUDP: Int = 8125
) extends DockerComposeYaml {
  lazy val get: String =
    s"""version: '3'
        |services:
        |  graphite:
        |    image: graphiteapp/graphite-statsd
        |    container_name: graphite
        |    ports:
        |      - "$graphiteUIPort:80"        # Graphite web UI
        |      - "$graphiteTCP:2003"      # Graphite plaintext TCP receiver
        |      - "$statsdUDP:8125/udp"  # StatsD UDP receiver
        |    environment:
        |      - GRAPHITE_MAX_SIZE=100000000
        |      - GRAPHITE_STORAGE_DIR=/opt/graphite/storage
        |
        |  grafana:
        |    image: grafana/grafana
        |    container_name: grafana
        |    ports:
        |      - "$grafanaPort:3000"      # Grafana UI
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
