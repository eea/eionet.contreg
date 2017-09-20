FROM ubuntu:14.04

RUN apt-get update
RUN apt-get install -y virtuoso-opensource-6.1
RUN ln -s /var/lib/virtuoso-opensource-6.1/db /data

# Add Virtuoso config
ADD virtuoso.ini /virtuoso.ini
ADD 1_create_users.sql /1_create_users.sql
ADD 2_setup_full_text_indexing.sql /2_setup_full_text_indexing.sql

# Add dump_nquads_procedure
#ADD dump_nquads_procedure.sql /dump_nquads_procedure.sql

# Add Virtuoso log cleaning script
#ADD clean-logs.sh /clean-logs.sh

# Add startup script
ADD virtuoso.sh /virtuoso.sh

VOLUME /data
WORKDIR /data
EXPOSE 8890
EXPOSE 1111

#RUN bash /virtuoso.sh
# CMD ["/usr/bin/virtuoso-t", "+wait", "+foreground", "+configfile", "/virtuoso.ini"]
CMD ["/bin/bash", "/virtuoso.sh"]