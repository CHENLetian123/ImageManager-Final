import { getImageContent } from '../api/imageApi'

export async function copyImage(image) {
  const url = image.publicUrl
  if (!image?.id && !url) {
    return 'No image is available.'
  }

  try {
    if (!navigator.clipboard || !window.ClipboardItem) {
      throw new Error('Clipboard image write is not supported.')
    }
    const { data } = await getImageContent(image.id)
    const blob = await normalizeClipboardBlob(data)
    await navigator.clipboard.write([
      new ClipboardItem({ [blob.type || 'image/png']: blob })
    ])
    return 'Copied image to clipboard'
  } catch (imageCopyError) {
    try {
      if (!url || !navigator.clipboard?.writeText) {
        throw new Error('Clipboard text write is not supported.')
      }
      await navigator.clipboard.writeText(url)
      return 'Image copy failed. Image link copied instead.'
    } catch (linkCopyError) {
      triggerDownload(image)
      return 'Clipboard is not available. Download started.'
    }
  }
}

async function normalizeClipboardBlob(blob) {
  if (blob.type === 'image/png') {
    return blob
  }

  try {
    return await convertBlobToPng(blob)
  } catch (error) {
    return blob
  }
}

function convertBlobToPng(blob) {
  return new Promise((resolve, reject) => {
    const image = new Image()
    const objectUrl = URL.createObjectURL(blob)
    image.onload = () => {
      const canvas = document.createElement('canvas')
      canvas.width = image.naturalWidth
      canvas.height = image.naturalHeight
      const context = canvas.getContext('2d')
      context.drawImage(image, 0, 0)
      canvas.toBlob((pngBlob) => {
        URL.revokeObjectURL(objectUrl)
        if (pngBlob) {
          resolve(pngBlob)
        } else {
          reject(new Error('Canvas conversion failed.'))
        }
      }, 'image/png')
    }
    image.onerror = () => {
      URL.revokeObjectURL(objectUrl)
      reject(new Error('Image decode failed.'))
    }
    image.src = objectUrl
  })
}

function triggerDownload(image) {
  const link = document.createElement('a')
  link.href = image.publicUrl || `/api/images/${image.id}/download`
  link.download = image.originalFileName || 'image'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}
