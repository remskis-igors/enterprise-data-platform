using Confluent.Kafka;
using Microsoft.Extensions.Logging;
using System.Threading.Tasks;

namespace CSharpGateway.Services
{
    public class KafkaProducerService : IKafkaProducerService
    {
        private readonly ILogger<KafkaProducerService> _logger;
        private readonly IProducer<Null, string> _producer;

        public KafkaProducerService(ILogger<KafkaProducerService> logger)
        {
            _logger = logger;

            var config = new ProducerConfig
            {
                BootstrapServers = "kafka:9092" // Kafka host inside Docker network
            };

            _producer = new ProducerBuilder<Null, string>(config).Build();
        }

        public async Task ProduceAsync(string topic, string message)
        {
            var result = await _producer.ProduceAsync(topic, new Message<Null, string> { Value = message });
            _logger.LogInformation("Message produced to Kafka: {0}", result.TopicPartitionOffset);
        }
    }
}
