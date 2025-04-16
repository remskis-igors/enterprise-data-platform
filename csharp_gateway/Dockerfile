FROM mcr.microsoft.com/dotnet/aspnet:5.0 AS base
WORKDIR /app
EXPOSE 80

FROM mcr.microsoft.com/dotnet/sdk:5.0 AS build
WORKDIR /src
COPY ["csharp-gateway.csproj", "./"]
RUN dotnet restore "csharp-gateway.csproj"
COPY . .
WORKDIR "/src/."
RUN dotnet build "csharp-gateway.csproj" -c Release -o /app/build

FROM build AS publish
RUN dotnet publish "csharp-gateway.csproj" -c Release -o /app/publish

FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "csharp-gateway.dll"]
