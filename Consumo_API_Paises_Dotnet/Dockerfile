FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src
COPY Consumo_API_Paises_Dotnet/Consumo_API_Paises_Dotnet.csproj ./
RUN dotnet restore Consumo_API_Paises_Dotnet.csproj
COPY Consumo_API_Paises_Dotnet/ ./
RUN dotnet publish Consumo_API_Paises_Dotnet.csproj -c Release --no-restore -o /out /p:UseAppHost=false

FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS runtime
WORKDIR /app
COPY --from=build /out ./
RUN mkdir -p /home/app/.aspnet/DataProtection-Keys \
    && chown -R $APP_UID:$APP_UID /home/app/.aspnet
ENV ASPNETCORE_HTTP_PORTS=8080
USER $APP_UID
EXPOSE 8080
ENTRYPOINT ["dotnet", "Consumo_API_Paises_Dotnet.dll"]
