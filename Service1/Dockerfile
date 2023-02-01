FROM python:latest

RUN wget https://140.82.121.4/openfaas/faas/releases/download/0.9.14/fwatchdog > /usr/bin/fwatchdog
RUN chmod +x /usr/bin/fwatchdog

ENV fprocess="python2 entrypoint.py"
COPY entrypoint.py /

EXPOSE 8080
CMD [ "fwatchdog" ]
