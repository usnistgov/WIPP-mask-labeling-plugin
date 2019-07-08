# README

## WIPP-mask-labeling-plugin

Assigns unique labels to contiguous sets of pixels in binary images (masks). The output images are tiled TIFFs.

## Docker distribution

This plugin is available on [DockerHub from the WIPP organization](https://hub.docker.com/r/wipp/wipp-mask-labeling-plugin)
```shell
docker pull wipp/wipp-mask-labeling-plugin
```

## Running the Docker container

```shell
docker run \
    -v "path/to/input/data/folder":/data/inputs \
    -v "path/to/output/folder":/data/outputs \
    wipp-image-mask-labeling-plugin \
    --inputImages /data/inputs/"inputCollectionTiledFolder"  
    --output /data/outputs
    --connectedness FOUR_CONNECTED
 ```   
