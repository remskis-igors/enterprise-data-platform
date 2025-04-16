using System.Threading.Tasks;

namespace CSharpGateway.Services
{
    public interface IKafkaProducerService
    {
        Task ProduceAsync(string topic, string message);
    }
}
