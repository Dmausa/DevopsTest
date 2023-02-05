FROM python:3.8

RUN curl -sL https://github.com/openfaas/faas/releases/download/0.9.14/fwatchdog > /usr/bin/fwatchdog \
    && chmod +x /usr/bin/fwatchdog

ENV fprocess="python entrypoint.py"
ENV SERVICE1_URL="http://service.example.com:8080"
COPY entrypoint.py /

RUN pip install requests

EXPOSE 8080
CMD [ "fwatchdog" ]