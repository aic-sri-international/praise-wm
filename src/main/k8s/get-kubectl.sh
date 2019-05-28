#!/bin/sh

# Script to retrieve kubectl and kustomize into the given directory.

set -e

if [ $# -ne 1 ] ; then
    echo "usage: $0 dir"
    exit 1
fi

DEST=$1

mkdir -p ${DEST}
cd ${DEST}

if [ ! -x kubectl ] ; then
    apk add curl
    K8S_VER=$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)
    curl -LO https://storage.googleapis.com/kubernetes-release/release/${K8S_VER}/bin/linux/amd64/kubectl
    chmod +x kubectl
fi

if [ ! -x kustomize ] ; then
    apk add curl
    curl -s https://api.github.com/repos/kubernetes-sigs/kustomize/releases/latest | \
        grep browser_download | \
        grep linux | \
        awk '{print $2}' | \
        xargs curl -LO
    mv kustomize_*_linux_amd64 kustomize
    chmod +x kustomize
fi
