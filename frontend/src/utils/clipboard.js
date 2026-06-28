export async function copyImage(image) {
  const url = image.publicUrl
  if (!url) {
    return 'No public image URL is available.'
  }

  try {
    const response = await fetch(url, { mode: 'cors' })
    const blob = await response.blob()
    if (navigator.clipboard && window.ClipboardItem) {
      await navigator.clipboard.write([
        new ClipboardItem({ [blob.type || 'image/png']: blob })
      ])
      return 'Copied image to clipboard'
    }
    throw new Error('Clipboard image write is not supported.')
  } catch (imageCopyError) {
    try {
      await navigator.clipboard.writeText(url)
      return 'Clipboard image copy failed. Image link copied instead.'
    } catch (linkCopyError) {
      const link = document.createElement('a')
      link.href = url
      link.download = image.originalFileName || 'image'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      return 'Download started'
    }
  }
}
